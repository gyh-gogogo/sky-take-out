package com.sky.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sky.thread")
public class ThreadProperties {
    /**
     * 线程池核心线程数
     */
    private Integer corePoolSize = 10;

    /**
     * 线程池最大线程数
     */
    private Integer maxPoolSize = 20;

    /**
     * 线程池队列容量
     */
    private Integer queueCapacity = 100;

    /**
     * 线程池前缀
     */
    private String threadNamePrefix = "sky-thread-";

}
