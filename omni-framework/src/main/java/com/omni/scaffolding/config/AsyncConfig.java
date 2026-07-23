package com.omni.scaffolding.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 打开 Spring 异步与缓存注解支持。
 *
 * <p>具体执行器由 {@link VirtualThreadConfig} 提供；缓存实现由 {@link RedisConfig}（或 test 下的 simple cache）提供。
 */
@Configuration
@EnableAsync
@EnableCaching
public class AsyncConfig {
}
