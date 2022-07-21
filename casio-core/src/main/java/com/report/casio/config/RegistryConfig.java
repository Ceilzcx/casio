package com.report.casio.config;

import com.report.casio.common.utils.StringUtil;
import lombok.Data;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class RegistryConfig {
    private String address;
    private String username;
    private String password;
    private String version;
    private String protocol;

    public List<String> getHosts() {
        if (StringUtil.isBlank(address)) {
            return Lists.newArrayList();
        }
        return new ArrayList<>(Arrays.asList(address.split(",")));
    }

}
