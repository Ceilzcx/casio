package com.report.casio.spring.ioc;

import com.report.casio.spring.annotation.EnableRegisterScan;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author hujiaofen
 * @since 19/7/2022
 */
public class ServiceRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<String> packageToScan = getPackageToScan(importingClassMetadata);
        registerServiceBeanDefinitions(packageToScan, registry);
    }

    private void registerServiceBeanDefinitions(Set<String> packageToScan, BeanDefinitionRegistry registry) {
        CasioClassPathScanner scanner = new CasioClassPathScanner(registry);
        scanner.registerFilters();
        for (String basePackage :packageToScan){
            // doScan() 底层实现了registry.registerBeanDefinition操作
            scanner.doScan(basePackage);
        }
    }

    private Set<String> getPackageToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(EnableRegisterScan.class.getName()));
        assert attributes != null;
        String[] basePackages = attributes.getStringArray("basePackages");
        Set<String> result = new HashSet<>(Arrays.asList(basePackages));
        if (result.isEmpty()) {
            return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return result;
    }
}
