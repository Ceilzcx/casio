package com.report.casio.spring.annotation;

import com.report.casio.spring.ioc.ConfigRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author hujiaofen
 * @since 19/7/2022
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(ConfigRegister.class)
public @interface EnableCasioConfig {
}
