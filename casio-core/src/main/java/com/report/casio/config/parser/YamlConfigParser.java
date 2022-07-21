package com.report.casio.config.parser;

import com.report.casio.common.Constants;
import com.report.casio.common.exception.ContextException;
import com.report.casio.config.ConsumerConfig;
import com.report.casio.config.ProviderConfig;
import com.report.casio.config.RegistryConfig;
import com.report.casio.config.context.RpcConfigContext;
import com.report.casio.config.context.RpcContextFactory;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Slf4j
public class YamlConfigParser implements ConfigParser {

    @Override
    public RpcConfigContext parse(String path) throws ContextException {
        Yaml yaml = new Yaml();
        InputStream inputStream = YamlConfigParser.class
                .getClassLoader()
                .getResourceAsStream(path);
        Map<String, Object> obj = yaml.load(inputStream);

        int providerPort = Constants.DEFAULT_PROVIDER_PORT;
        int providerTimeout = Constants.DEFAULT_TIMEOUT;
        String registryAddress = Constants.DEFAULT_REGISTRY_ADDRESS;
        String registryProtocol = Constants.DEFAULT_REGISTRY_REGISTRY_PROTOCOL;

        try {
            if (obj.containsKey(Constants.PROJECT)) {
                Map<String, Object> configs = (Map<String, Object>) obj.get(Constants.PROJECT);
                Map<String, Object> providerConfig = (Map<String, Object>) configs.get(Constants.PROVIDER);
                if (providerConfig != null) {
                    if (providerConfig.containsKey(Constants.PORT)) {
                        providerPort = Integer.parseInt(providerConfig.get(Constants.PORT).toString());
                    }
                }
                Map<String, Object> registryConfig = (Map<String, Object>) configs.get(Constants.REGISTRY);
                if (registryConfig != null) {
                    if (registryConfig.containsKey(Constants.ADDRESS)) {
                        registryAddress = registryConfig.get(Constants.ADDRESS).toString();
                    }
                    if (registryConfig.containsKey(Constants.PROTOCOL)) {
                        registryProtocol = registryConfig.get(Constants.PROTOCOL).toString();
                    }
                    if (registryConfig.containsKey(Constants.TIMEOUT)) {
                        providerTimeout = Integer.parseInt(registryConfig.get(Constants.TIMEOUT).toString());
                    }
                }
            }

            ProviderConfig providerConfig = new ProviderConfig(providerPort, providerTimeout);
            ConsumerConfig consumerConfig = new ConsumerConfig();
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(registryAddress);
            registryConfig.setProtocol(registryProtocol);
            return RpcContextFactory.createConfigContext(providerConfig, consumerConfig, registryConfig);
        } catch (Exception e) {
            throw new ContextException(path + " parser error", e.getCause());
        }
    }

}
