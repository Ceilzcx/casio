package com.report.casio.spring.test.controller;

import com.report.casio.common.annotation.Reference;
import com.report.casio.spring.test.service.IDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hujiaofen
 * @since 19/7/2022
 */
@RestController
@RequestMapping("/demo")
public class DemoController {
    @Reference
    private IDemoService demoService;

    @GetMapping("/sayHello")
    public String sayHello(@RequestParam("name") String name) {
        return demoService.sayHello(name);
    }

}
