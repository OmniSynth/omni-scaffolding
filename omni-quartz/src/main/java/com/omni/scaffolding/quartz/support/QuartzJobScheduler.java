package com.omni.scaffolding.quartz.support;

import com.omni.scaffolding.quartz.job.BeanInvokeJob;
import com.omni.scaffolding.quartz.job.DisallowConcurrentBeanInvokeJob;
import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 管理台任务与 Quartz Scheduler 的同步封装。
 *
 * <p>勿用 {@code @ConditionalOnBean(Scheduler)}：组件扫描阶段早于
 * {@code QuartzAutoConfiguration}，会导致本 Bean 永远不创建。
 */
@Component
@ConditionalOnProperty(prefix = "omni.quartz", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class QuartzJobScheduler {

    /**
     * 未指定任务组时的默认 Quartz Job 组名。
     */
    public static final String DEFAULT_GROUP = "omni-job";

    private final Scheduler scheduler;

    /**
     * 创建或更新 Job / Cron Trigger，并与 {@link ManagedJobDefinition} 保持一致。
     *
     * <p>已存在则先删后建，确保并发策略与 JobDataMap 同步；{@code enabled=false} 时注册后立即暂停。
     *
     * @param def 任务定义（与 sys_job 字段对齐）
     * @throws SchedulerException Quartz 调度失败
     */
    public void createOrUpdate(ManagedJobDefinition def) throws SchedulerException {
        JobKey jobKey = jobKey(def.jobId(), def.jobGroup());
        TriggerKey triggerKey = triggerKey(def.jobId(), def.jobGroup());

        JobDetail jobDetail = JobBuilder.newJob(jobClass(def.concurrent()))
                .withIdentity(jobKey)
                .withDescription(def.jobName())
                .usingJobData(JobDataKeys.JOB_ID, def.jobId())
                .usingJobData(JobDataKeys.JOB_NAME, def.jobName())
                .usingJobData(JobDataKeys.INVOKE_TARGET, def.invokeTarget())
                .usingJobData(JobDataKeys.JOB_PARAMS, def.jobParams() == null ? "" : def.jobParams())
                .storeDurably()
                .build();

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(def.cronExpression());
        applyMisfire(scheduleBuilder, def.misfirePolicy());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withDescription("Cron: " + def.cronExpression())
                .forJob(jobDetail)
                .withSchedule(scheduleBuilder)
                .build();

        // 删除后重建，确保 Job 实现类（是否禁止并发）与 JobDataMap 始终与元数据一致
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
        scheduler.scheduleJob(jobDetail, Set.of(trigger), true);

        if (!def.enabled()) {
            scheduler.pauseJob(jobKey);
        }
    }

    /**
     * 暂停指定任务（不再触发，Job 定义仍保留）。
     *
     * @param jobId    任务 ID
     * @param jobGroup 任务组，空则使用 {@link #DEFAULT_GROUP}
     */
    public void pause(Long jobId, String jobGroup) throws SchedulerException {
        scheduler.pauseJob(jobKey(jobId, jobGroup));
    }

    /**
     * 恢复已暂停的任务。
     *
     * @param jobId    任务 ID
     * @param jobGroup 任务组，空则使用 {@link #DEFAULT_GROUP}
     */
    public void resume(Long jobId, String jobGroup) throws SchedulerException {
        scheduler.resumeJob(jobKey(jobId, jobGroup));
    }

    /**
     * 从 Scheduler 删除任务（含 Trigger）；不存在则忽略。
     *
     * @param jobId    任务 ID
     * @param jobGroup 任务组，空则使用 {@link #DEFAULT_GROUP}
     */
    public void delete(Long jobId, String jobGroup) throws SchedulerException {
        JobKey key = jobKey(jobId, jobGroup);
        if (scheduler.checkExists(key)) {
            scheduler.deleteJob(key);
        }
    }

    /**
     * 立即触发一次执行（管理台「运行一次」）。
     *
     * <p>Scheduler 中尚无该 Job 时会先 {@link #createOrUpdate(ManagedJobDefinition)}。
     *
     * @param def 任务定义
     */
    public void triggerOnce(ManagedJobDefinition def) throws SchedulerException {
        JobKey key = jobKey(def.jobId(), def.jobGroup());
        if (!scheduler.checkExists(key)) {
            createOrUpdate(def);
        }
        scheduler.triggerJob(key, jobDetailData(def));
    }

    /**
     * 校验 Cron 表达式是否合法。
     *
     * @param cron Quartz Cron 表达式
     * @return 合法返回 {@code true}
     */
    public static boolean isValidCron(String cron) {
        return cron != null && CronExpression.isValidExpression(cron.trim());
    }

    /**
     * 预览后续若干次触发时间（系统默认时区，格式 {@code yyyy-MM-dd HH:mm:ss}）。
     *
     * @param cron  Cron 表达式
     * @param count 预览次数，≤0 或表达式非法时返回空列表
     * @return 触发时间字符串列表
     */
    public static List<String> nextFireTimes(String cron, int count) {
        if (!isValidCron(cron) || count <= 0) {
            return List.of();
        }
        try {
            CronExpression expression = new CronExpression(cron.trim());
            expression.setTimeZone(java.util.TimeZone.getTimeZone(ZoneId.systemDefault()));
            List<String> times = new ArrayList<>();
            Date cursor = new Date();
            for (int i = 0; i < count; i++) {
                Date next = expression.getNextValidTimeAfter(cursor);
                if (next == null) {
                    break;
                }
                times.add(next.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toString().replace('T', ' '));
                cursor = next;
            }
            return times;
        } catch (ParseException ex) {
            return List.of();
        }
    }

    /**
     * 构建单次触发用的 JobDataMap（与持久化 Job 定义字段一致）。
     *
     * @param def 任务定义
     * @return Quartz JobDataMap
     */
    private static org.quartz.JobDataMap jobDetailData(ManagedJobDefinition def) {
        org.quartz.JobDataMap map = new org.quartz.JobDataMap();
        map.put(JobDataKeys.JOB_ID, def.jobId());
        map.put(JobDataKeys.JOB_NAME, def.jobName());
        map.put(JobDataKeys.INVOKE_TARGET, def.invokeTarget());
        map.put(JobDataKeys.JOB_PARAMS, def.jobParams() == null ? "" : def.jobParams());
        return map;
    }

    /**
     * 按是否允许并发选择 Job 实现类。
     *
     * @param concurrent {@code true} 允许并发执行
     * @return Quartz Job 类型
     */
    private static Class<? extends org.quartz.Job> jobClass(boolean concurrent) {
        return concurrent ? BeanInvokeJob.class : DisallowConcurrentBeanInvokeJob.class;
    }

    /**
     * 按 misfire 策略配置 Cron 调度器。
     *
     * @param builder Cron 调度构建器
     * @param policy  策略码：0 忽略，1 立即触发，2 全部补偿
     */
    private static void applyMisfire(CronScheduleBuilder builder, int policy) {
        switch (policy) {
            case 1 -> builder.withMisfireHandlingInstructionFireAndProceed();
            case 2 -> builder.withMisfireHandlingInstructionIgnoreMisfires();
            default -> builder.withMisfireHandlingInstructionDoNothing();
        }
    }

    /**
     * 构建 Quartz JobKey。
     *
     * @param jobId 任务 ID
     * @param group 任务组
     * @return JobKey
     */
    private static JobKey jobKey(Long jobId, String group) {
        return JobKey.jobKey(String.valueOf(jobId), resolveGroup(group));
    }

    /**
     * 构建 Quartz TriggerKey。
     *
     * @param jobId 任务 ID
     * @param group 任务组
     * @return TriggerKey
     */
    private static TriggerKey triggerKey(Long jobId, String group) {
        return TriggerKey.triggerKey("trigger-" + jobId, resolveGroup(group));
    }

    /**
     * 解析任务组名，空值时使用默认组。
     *
     * @param group 原始组名
     * @return 非空组名
     */
    private static String resolveGroup(String group) {
        return group == null || group.isBlank() ? DEFAULT_GROUP : group.trim();
    }
}
