package com.report.casio.config.context;

import com.report.casio.config.ConsumerConfig;
import com.report.casio.config.ProviderConfig;
import com.report.casio.config.RegistryConfig;

public class RpcConfigContext {
    private RegistryConfig registryConfig;
    private ProviderConfig providerConfig;
    private ConsumerConfig consumerConfig;

    protected RpcConfigContext() {
    }

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public ConsumerConfig getConsumerConfig() {
        return consumerConfig;
    }

    protected void setConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    protected void setProviderConfig(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }
}
