package com.omni.scaffolding.quartz;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Quartz 开关与演示任务配置。
 *
 * <p>默认 {@code enabled=true}（集群 JDBC 模式）；不需要定时任务时设为 {@code false}。
 */
@Data
@Component
@ConfigurationProperties(prefix = "omni.quartz")
public class OmniQuartzProperties {

    /**
     * 是否启用 Quartz。
     * <p>false 时由 {@link QuartzEnableEnvironmentPostProcessor} 排除自动配置。
     */
    private boolean enabled = true;

    /**
     * 是否注册脚手架演示心跳任务。
     */
    private boolean demoJobEnabled = true;

    /**
     * 演示任务 Cron（默认每分钟整点）。
     */
    private String demoCron = "0 * * * * ?";
}
