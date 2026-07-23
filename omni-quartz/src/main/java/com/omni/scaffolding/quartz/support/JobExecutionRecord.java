package com.omni.scaffolding.quartz.support;

import java.time.Instant;

/**
 * 一次任务执行结果，由 {@link JobExecutionRecorder} 落库。
 *
 * @param jobId        任务 ID
 * @param jobName      任务名称
 * @param invokeTarget 调用目标
 * @param jobParams    任务参数
 * @param success      是否成功
 * @param message      结果摘要或错误信息
 * @param startTime    开始时间
 * @param endTime      结束时间
 * @param costMs       耗时（毫秒）
 */
public record JobExecutionRecord(
        Long jobId,
        String jobName,
        String invokeTarget,
        String jobParams,
        boolean success,
        String message,
        Instant startTime,
        Instant endTime,
        long costMs
) {
}
