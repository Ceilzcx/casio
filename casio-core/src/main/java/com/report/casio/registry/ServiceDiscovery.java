package com.report.casio.registry;

import com.report.casio.common.annotation.SPI;
import com.report.casio.domain.RpcRequest;

import java.net.InetSocketAddress;

// todo com.report.casio.config.RegistryConfig.protocol
@SPI("zookeeper")
public interface ServiceDiscovery {

    // 服务发现接口
    InetSocketAddress lookup(RpcRequest rpcRequest);

}
