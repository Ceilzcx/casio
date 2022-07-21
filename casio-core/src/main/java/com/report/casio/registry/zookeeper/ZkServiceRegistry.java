package com.report.casio.registry.zookeeper;

import com.report.casio.common.utils.StringUtil;
import com.report.casio.config.context.RpcContextFactory;
import com.report.casio.config.ServiceConfig;
import com.report.casio.registry.ServiceRegistry;

public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void register(ServiceConfig serviceConfig) throws Exception {
        boolean res = false;
        for (String host : RpcContextFactory.getConfigContext().getRegistryConfig().getHosts()) {
            if (ZkUtils.create(host,
                    StringUtil.generateProviderPath(serviceConfig.getServiceName(), RpcContextFactory.getConfigContext().getProviderConfig().getHost()),
                    null)) {
                res = true;
            }
        }
        if (res) {
            RpcContextFactory.getBeanContext().registerService(serviceConfig.getServiceName(), Class.forName(serviceConfig.getRef()).newInstance());
        }
    }
}
