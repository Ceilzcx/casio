package com.report.casio.spring.test.service;

import com.report.casio.common.annotation.Register;

/**
 * @author hujiaofen
 * @since 19/7/2022
 */
@Register
public class DemoServiceImpl implements IDemoService{
    @Override
    public String sayHello(String s) {
        return "hello, " + s;
    }
}
