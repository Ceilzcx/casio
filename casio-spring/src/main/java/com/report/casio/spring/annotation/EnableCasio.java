package com.report.casio.spring.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author hujiaofen
 * @since 19/7/2022
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnableRegisterScan
@EnableCasioConfig
public @interface EnableCasio {
    @AliasFor(annotation = EnableRegisterScan.class, attribute = "basePackages")
    String[] scanBasePackages() default {};
}
