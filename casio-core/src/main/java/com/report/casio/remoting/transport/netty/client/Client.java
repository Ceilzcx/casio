package com.report.casio.remoting.transport.netty.client;

import com.report.casio.common.exception.RemotingException;
import com.report.casio.remoting.transport.netty.TimerChannel;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public interface Client {

    Channel doConnect(InetSocketAddress inetSocketAddress) throws RemotingException;

    void doClose();

    TimerChannel getChannel(InetSocketAddress inetSocketAddress) throws RemotingException;

    void reconnect(Channel channel);

}
