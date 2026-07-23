package com.omni.scaffolding.quartz;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 集群装配（仅 {@code omni.quartz.enabled=true} 时生效）。
 *
 * <p>JobStore / 集群参数见 {@code spring.quartz.*}；此处仅做轻量定制。
 *
 * <h2>多实例约定</h2>
 * <ul>
 *   <li>各节点共用同一 MySQL 与 {@code QRTZ_*} 表</li>
 *   <li>同一 Trigger 靠 DB 行锁互斥，仅一台触发</li>
 *   <li>长任务 Job 请加 {@code @DisallowConcurrentExecution}，并保证业务幂等</li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(prefix = "omni.quartz", name = "enabled", havingValue = "true", matchIfMissing = true)
public class QuartzConfig {

    /**
     * 定制 SchedulerFactoryBean：覆盖已有 Job、延迟启动、优雅停机等待任务结束。
     */
    @Bean
    public SchedulerFactoryBeanCustomizer omniSchedulerCustomizer() {
        return factory -> {
            factory.setOverwriteExistingJobs(true);
            factory.setStartupDelay(5);
            factory.setWaitForJobsToCompleteOnShutdown(true);
        };
    }
}
