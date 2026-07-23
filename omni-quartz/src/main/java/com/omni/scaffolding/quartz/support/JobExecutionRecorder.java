package com.omni.scaffolding.quartz.support;

/**
 * 任务执行日志记录器；由业务模块（omni-system）实现并落库。
 */
public interface JobExecutionRecorder {

    /**
     * 记录一次任务执行结果（由 omni-system 等模块落库）。
     *
     * @param record 执行摘要
     */
    void record(JobExecutionRecord record);
}
