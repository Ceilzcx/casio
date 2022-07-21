package com.report.casio.common.exception;

import io.netty.channel.Channel;

import java.net.SocketAddress;

// remoting相关自定义异常类
public class RemotingException extends Exception {
    private final SocketAddress localAddress;
    private final SocketAddress remoteAddress;

    public RemotingException(Channel channel, String message) {
        this(channel.localAddress(), channel.remoteAddress(), message);
    }

    public RemotingException(SocketAddress localAddress, SocketAddress remoteAddress, String message) {
        super(message);
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public SocketAddress getLocalAddress() {
        return localAddress;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
