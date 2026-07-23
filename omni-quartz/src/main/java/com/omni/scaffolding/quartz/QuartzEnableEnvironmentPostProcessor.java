package com.omni.scaffolding.quartz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
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
 * 根据 {@code omni.quartz.enabled} 决定是否排除 Quartz 自动配置。
 *
 * <p>关闭后不启动 Scheduler、不访问 {@code QRTZ_*} 表，适合无定时任务场景。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 21)
public class QuartzEnableEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /** 开关配置键：{@code omni.quartz.enabled}。 */
    public static final String ENABLED_KEY = "omni.quartz.enabled";
    private static final String EXCLUDE_KEY = "spring.autoconfigure.exclude";
    private static final String QUARTZ_AUTO_CONFIG = QuartzAutoConfiguration.class.getName();

    /**
     * {@code omni.quartz.enabled=false} 时将 {@link QuartzAutoConfiguration} 加入排除列表。
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 默认开启；仅显式 false 时排除
        boolean enabled = environment.getProperty(ENABLED_KEY, Boolean.class, true);
        if (enabled) {
            return;
        }

        Set<String> excludes = new LinkedHashSet<>(readExcludes(environment.getProperty(EXCLUDE_KEY)));
        excludes.add(QUARTZ_AUTO_CONFIG);

        Map<String, Object> map = new HashMap<>(1);
        map.put(EXCLUDE_KEY, String.join(",", excludes));
        environment.getPropertySources().addFirst(new MapPropertySource("omniQuartzDisable", map));
    }

    /**
     * 解析 {@code spring.autoconfigure.exclude} 逗号分隔配置为列表。
     *
     * @param raw 原始配置字符串
     * @return 去重前的排除类名列表
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
