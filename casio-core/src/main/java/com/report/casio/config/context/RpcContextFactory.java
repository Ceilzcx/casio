package com.report.casio.config.context;

import com.report.casio.config.ConsumerConfig;
import com.report.casio.config.ProviderConfig;
import com.report.casio.config.RegistryConfig;

public class RpcContextFactory {
    private static RpcConfigContext configContext;
    private static final BeanContext beanContext = new BeanContext();

    private RpcContextFactory() {
    }

    public static RpcConfigContext createConfigContext(ProviderConfig providerConfig,
                                                       ConsumerConfig consumerConfig,
                                                       RegistryConfig registryConfig) {
        if (configContext == null) {
            // 饿汉模式双空指针判断
            synchronized (RpcContextFactory.class) {
                configContext = new RpcConfigContext();
                configContext.setProviderConfig(providerConfig);
                configContext.setConsumerConfig(consumerConfig);
                configContext.setRegistryConfig(registryConfig);
            }
        }
        return configContext;
    }

    public static RpcConfigContext getConfigContext() {
        return configContext;
    }

    public static BeanContext getBeanContext() {
        return beanContext;
    }
}
