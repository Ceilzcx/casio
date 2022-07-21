package com.report.casio.test;

import com.report.casio.boostrap.Bootstraps;
import com.report.casio.embed.EmbeddedZookeeper;
import com.report.casio.rpc.proxy.RpcProxyUtil;
import com.report.casio.test.service.DemoServiceImpl;
import com.report.casio.test.service.IDemoService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author hujiaofen
 * @since 15/7/2022
 */
public class ApplicationTest {

    public static void main(String[] args) {
        new EmbeddedZookeeper(2181, false, "C:\\Ceilzcx\\project\\data\\zookeeper").start();
        try {
            // zk启动等待时间
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        Bootstraps bootstraps = new Bootstraps();
        bootstraps.start();
        bootstraps.registerService(DemoServiceImpl.class);

        try {
            // casio启动等待时间
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        IDemoService service = RpcProxyUtil.getProxy(IDemoService.class);
        assert service != null;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
        executorService.scheduleAtFixedRate(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(service.sayHello());
            }
        }, 0, 20, TimeUnit.SECONDS);
    }

}
