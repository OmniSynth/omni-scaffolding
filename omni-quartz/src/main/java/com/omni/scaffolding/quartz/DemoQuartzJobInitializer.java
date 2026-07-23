package com.omni.scaffolding.quartz;

import com.omni.scaffolding.quartz.job.DemoHeartbeatJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 启动时幂等注册演示 Job / Trigger。
 *
 * <p>{@code scheduleJob(..., replace=true)} 配合 {@code overwrite-existing-jobs}，
 * 多节点重复注册不会产生冲突。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "omni.quartz", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoQuartzJobInitializer implements ApplicationRunner {

    /** 演示 Job 名称。 */
    public static final String JOB_NAME = "demoHeartbeatJob";
    /** 演示 Job 组。 */
    public static final String JOB_GROUP = "omni-demo";
    /** 演示 Trigger 名称。 */
    public static final String TRIGGER_NAME = "demoHeartbeatTrigger";

    private final Scheduler scheduler;
    private final OmniQuartzProperties quartzProperties;

    /**
     * 启动时幂等注册 {@link DemoHeartbeatJob} 与 Cron Trigger。
     *
     * @param args 启动参数（未使用）
     */
    @Override
    public void run(ApplicationArguments args) throws SchedulerException {
        if (!quartzProperties.isDemoJobEnabled()) {
            log.info("Quartz demo job disabled via omni.quartz.demo-job-enabled=false");
            return;
        }

        JobDetail jobDetail = JobBuilder.newJob(DemoHeartbeatJob.class)
                .withIdentity(JOB_NAME, JOB_GROUP)
                .withDescription("Omni scaffolding demo heartbeat (cluster-safe)")
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(TRIGGER_NAME, JOB_GROUP)
                .withDescription("Cron: " + quartzProperties.getDemoCron())
                .forJob(jobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule(quartzProperties.getDemoCron())
                        .withMisfireHandlingInstructionDoNothing())
                .build();

        scheduler.scheduleJob(jobDetail, Set.of(trigger), true);
        log.info("Quartz demo job registered: job={}.{}, cron={}", JOB_GROUP, JOB_NAME, quartzProperties.getDemoCron());
    }
}
