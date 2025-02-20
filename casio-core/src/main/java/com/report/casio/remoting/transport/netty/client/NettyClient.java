package com.report.casio.remoting.transport.netty.client;

import com.report.casio.common.exception.RemotingException;
import com.report.casio.common.extension.ExtensionLoader;
import com.report.casio.common.utils.SystemUtil;
import com.report.casio.domain.RpcMessage;
import com.report.casio.domain.RpcRequest;
import com.report.casio.domain.RpcResponse;
import com.report.casio.registry.ServiceDiscovery;
import com.report.casio.remoting.transport.netty.RpcRequestTransport;
import com.report.casio.remoting.transport.netty.TimerChannel;
import com.report.casio.remoting.transport.netty.client.cache.ChannelClient;
import com.report.casio.remoting.transport.netty.client.cache.CompletableRequest;
import com.report.casio.remoting.transport.netty.codec.RpcMessageDecoder;
import com.report.casio.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class NettyClient implements Client, RpcRequestTransport {
    private final Bootstrap bootstrap;
    private final EventLoopGroup workGroup;
    private final Lock connectLock = new ReentrantLock();

    public NettyClient() {
        this.bootstrap = new Bootstrap();
        this.workGroup = SystemUtil.isLinux() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        bootstrap.group(workGroup)
                .channel(SystemUtil.isLinux() ? EpollSocketChannel.class : NioSocketChannel.class)
                // 设置客户端调用服务端接口的超时时间5s
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        // 心跳机制
                        pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyClientHandler());
                    }
                });
    }

    @Override
    // connect方法线程安全，连接会创建一个新的NioSocketChannel
    public Channel doConnect(InetSocketAddress inetSocketAddress) throws RemotingException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        try {
            bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client connect success, address: {}", inetSocketAddress.toString());
                    completableFuture.complete(future.channel());
                } else {
                    throw new RemotingException(future.channel(), "client connect failed");
                }
            }).sync();
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new RemotingException(null, inetSocketAddress, "client connect failed");
        }
    }

    @Override
    public void doClose() {
        if (workGroup != null) workGroup.shutdownGracefully();
    }

    @Override
    public TimerChannel getChannel(InetSocketAddress inetSocketAddress) throws RemotingException {
        TimerChannel timerChannel = ChannelClient.get(inetSocketAddress);
        if (timerChannel == null) {
            ChannelClient.put(inetSocketAddress, new TimerChannel(doConnect(inetSocketAddress)));
        }
        return ChannelClient.get(inetSocketAddress);
    }

    @Override
    public void reconnect(Channel channel) {
        try {
            connectLock.lock();
            SocketAddress localAddress = channel.localAddress();
            ChannelFuture future = channel.close();
            if (future.isSuccess()) {
                try {
                    doConnect((InetSocketAddress) localAddress);
                } catch (RemotingException e) {
                    log.error("reconnect remoteAddress: {} error, localAddress: {}", e.getRemoteAddress(), e.getLocalAddress());
                }
            } else {
                log.error(localAddress + " close error");
            }
        } finally {
            connectLock.unlock();
        }
    }

    @Override
    @SneakyThrows
    public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse> completableFuture = new CompletableFuture<>();

        ServiceDiscovery serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getDefaultExtension();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookup(rpcRequest);
        if (inetSocketAddress == null) {
            log.error("service {} get failed", rpcRequest.getServiceName());
            return completableFuture;
        }

        TimerChannel timerChannel = getChannel(inetSocketAddress);
        Channel channel = timerChannel.getChannel();
        if (channel.isActive()) {
            timerChannel.setLastRead();
            CompletableRequest.put(rpcRequest.getRequestId(), completableFuture);
            RpcMessage rpcMessage = new RpcMessage(rpcRequest);
            log.info("send message to {}", inetSocketAddress);
            if (channel.isWritable()) {
                channel.writeAndFlush(rpcMessage);
            } else {
                log.warn("发送消息队列busy，已达到最高水位，发送失败");
            }
        } else {
            ChannelClient.remove(inetSocketAddress);
            log.error("channel is not active");
        }

        return completableFuture;
    }

}
