package com.report.casio.common.utils;

import com.report.casio.common.Constants;

public class StringUtils {
    private static final String ZK_SEPARATOR = "/";

    private StringUtils() {}

    public static boolean isBlank(String s) {
        return s == null || s.isEmpty() || s.trim().isEmpty();
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static String generateProviderPath(String serviceName) {
        return ZK_SEPARATOR + Constants.PROJECT +
                ZK_SEPARATOR + serviceName +
                ZK_SEPARATOR + Constants.PROVIDER;
    }

    public static String generateProviderPath(String serviceName, String address) {
        return generateProviderPath(serviceName) +
                ZK_SEPARATOR + address;
    }

}
