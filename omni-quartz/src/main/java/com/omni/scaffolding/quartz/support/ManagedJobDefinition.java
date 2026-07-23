package com.omni.scaffolding.quartz.support;

/**
 * 管理台任务调度定义（与 sys_job 字段对齐的精简模型）。
 *
 * @param jobId          任务 ID
 * @param jobName        任务名称
 * @param jobGroup       Quartz 任务组，空则使用默认组
 * @param invokeTarget   调用目标，格式 {@code beanName.methodName}
 * @param jobParams      可选任务参数
 * @param cronExpression Cron 表达式
 * @param misfirePolicy  错过触发策略：0=忽略，1=立即补跑，2=全部补跑
 * @param concurrent     是否允许同一 Job 并发执行
 * @param enabled        是否启用（false 时注册后暂停）
 */
public record ManagedJobDefinition(
        Long jobId,
        String jobName,
        String jobGroup,
        String invokeTarget,
        String jobParams,
        String cronExpression,
        int misfirePolicy,
        boolean concurrent,
        boolean enabled
) {
}
