package com.report.casio.registry;

import com.report.casio.common.annotation.SPI;
import com.report.casio.config.ServiceConfig;

// todo com.report.casio.config.RegistryConfig.protocol
@SPI("zookeeper")
public interface ServiceRegistry {
    void register(ServiceConfig serviceConfig) throws Exception;
}
