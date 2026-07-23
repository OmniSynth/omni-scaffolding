package com.omni.scaffolding.quartz.support;

/**
 * Quartz {@code JobDataMap} 键常量。
 */
public final class JobDataKeys {

    /**
     * 任务 ID（对应 sys_job.id）。
     */
    public static final String JOB_ID = "jobId";
    /**
     * 任务名称。
     */
    public static final String JOB_NAME = "jobName";
    /**
     * 调用目标，格式 {@code beanName.methodName}。
     */
    public static final String INVOKE_TARGET = "invokeTarget";
    /**
     * 可选任务参数，传给单 String 形参方法。
     */
    public static final String JOB_PARAMS = "jobParams";

    private JobDataKeys() {
    }
}
