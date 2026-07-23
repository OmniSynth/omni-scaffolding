package com.omni.scaffolding.config.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
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
 * 在配置文件加载后，根据 {@code omni.elasticsearch.enabled} 补充排除项与 Health 开关。
 *
 * <p>与 {@link ElasticsearchAutoConfigurationImportFilter} 双保险：关闭时不连 ES、不做健康探测。
 */
@Order(ConfigDataEnvironmentPostProcessor.ORDER + 20)
public class ElasticsearchEnableEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String ENABLED_KEY = "omni.elasticsearch.enabled";
    private static final String EXCLUDE_KEY = "spring.autoconfigure.exclude";
    private static final String HEALTH_KEY = "management.health.elasticsearch.enabled";

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 20;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean enabled = environment.getProperty(ENABLED_KEY, Boolean.class, false);
        if (enabled) {
            return;
        }

        Set<String> excludes = new LinkedHashSet<>(readExcludes(environment.getProperty(EXCLUDE_KEY, String[].class)));
        excludes.addAll(ElasticsearchAutoConfigurationImportFilter.ES_AUTO_CONFIGS);

        Map<String, Object> map = new HashMap<>(2);
        // 使用 String[]，确保 Binder / AutoConfigurationImportSelector 按数组消费
        map.put(EXCLUDE_KEY, excludes.toArray(String[]::new));
        map.put(HEALTH_KEY, false);
        environment.getPropertySources().addFirst(new MapPropertySource("omniElasticsearchDisable", map));
    }

    /**
     * 解析 {@code spring.autoconfigure.exclude} 配置（支持数组与逗号分隔）。
     *
     * @param raw 原始配置数组
     * @return 排除类名列表
     */
    private static List<String> readExcludes(String[] raw) {
        if (raw == null || raw.length == 0) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw)
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
