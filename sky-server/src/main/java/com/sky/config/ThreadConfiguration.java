package com.sky.config;

import com.sky.config.properties.ThreadProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(ThreadProperties.class)
public class ThreadConfiguration {
    @Autowired
    ThreadProperties threadProperties;
    // IO密集型
    @Bean
    public ThreadPoolExecutor ioThreadPool() {
        return new ThreadPoolExecutor(
                threadProperties.getCorePoolSize(), // core pool size
                threadProperties.getMaxPoolSize(), // max pool size
                threadProperties.getQueueCapacity(), // keep alive time
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
    }
}
