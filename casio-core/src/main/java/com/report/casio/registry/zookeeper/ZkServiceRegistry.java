package com.report.casio.registry.zookeeper;

import com.report.casio.common.utils.StringUtil;
import com.report.casio.config.RegistryConfig;
import com.report.casio.config.context.RpcContextFactory;
import com.report.casio.config.ServiceConfig;
import com.report.casio.registry.ServiceRegistry;

import java.util.List;

public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void register(ServiceConfig serviceConfig) throws Exception {
        List<RegistryConfig> registryConfigs = RpcContextFactory.getConfigContext().getRegistryConfigs();
        // todo：one register success result success？
        boolean res = false;
        for (RegistryConfig registryConfig : registryConfigs) {
            if (ZkUtils.create(registryConfig.getHost(),
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
