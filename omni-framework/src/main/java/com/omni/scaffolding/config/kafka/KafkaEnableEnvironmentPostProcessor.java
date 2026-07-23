package com.omni.scaffolding.config.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在 Spring 容器刷新前根据 {@code omni.kafka.enabled} 决定是否排除 Kafka 自动配置。
 *
 * <p>默认关闭：classpath 虽有 spring-kafka，但不会去连 Broker，适合「可选中间件」脚手架场景。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class KafkaEnableEnvironmentPostProcessor implements EnvironmentPostProcessor {

    public static final String ENABLED_KEY = "omni.kafka.enabled";
    private static final String EXCLUDE_KEY = "spring.autoconfigure.exclude";
    private static final String KAFKA_AUTO_CONFIG = KafkaAutoConfiguration.class.getName();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean enabled = environment.getProperty(ENABLED_KEY, Boolean.class, false);
        if (enabled) {
            return;
        }

        Set<String> excludes = new LinkedHashSet<>(readExcludes(environment.getProperty(EXCLUDE_KEY)));
        excludes.add(KAFKA_AUTO_CONFIG);

        Map<String, Object> map = new HashMap<>(1);
        map.put(EXCLUDE_KEY, String.join(",", excludes));
        environment.getPropertySources().addFirst(new MapPropertySource("omniKafkaDisable", map));
    }

    /**
     * 解析 {@code spring.autoconfigure.exclude} 逗号分隔配置为列表。
     *
     * @param raw 原始配置字符串
     * @return 排除类名列表
     */
    private static List<String> readExcludes(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
