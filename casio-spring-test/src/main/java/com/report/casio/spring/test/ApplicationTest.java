package com.report.casio.spring.test;

import com.report.casio.spring.annotation.EnableCasio;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hujiaofen
 * @since 19/7/2022
 */
@SpringBootApplication
@EnableCasio
public class ApplicationTest {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationTest.class, args);
    }

}
