package com.report.casio.spring.ioc;

import com.report.casio.boostrap.Bootstraps;
import com.report.casio.embed.EmbeddedZookeeper;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author hujiaofen
 * @since 19/7/2022
 */
public class ConfigRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        new EmbeddedZookeeper(2181, false, "C:\\Ceilzcx\\project\\data\\zookeeper").start();
        try {
            // zk启动等待时间
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        new Bootstraps().start();
    }
}
