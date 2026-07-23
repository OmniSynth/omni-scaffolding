package com.omni.scaffolding.quartz.job;

import com.omni.scaffolding.quartz.support.JobDataKeys;
import com.omni.scaffolding.quartz.support.JobExecutionRecord;
import com.omni.scaffolding.quartz.support.JobExecutionRecorder;
import com.omni.scaffolding.quartz.support.QuartzJobRegistry;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.Instant;

/**
 * 允许并发的 Bean 方法调用 Job。
 *
 * <p>从 JobDataMap 读取调用目标，经 {@link QuartzJobRegistry} 白名单执行；
 * 若存在 {@link JobExecutionRecorder} 则记录执行日志。
 */
@Slf4j
public class BeanInvokeJob extends QuartzJobBean {

    @Autowired
    private QuartzJobRegistry quartzJobRegistry;

    @Autowired(required = false)
    private JobExecutionRecorder executionRecorder;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        var data = context.getMergedJobDataMap();
        Long jobId = data.getLong(JobDataKeys.JOB_ID);
        String jobName = data.getString(JobDataKeys.JOB_NAME);
        String invokeTarget = data.getString(JobDataKeys.INVOKE_TARGET);
        String jobParams = data.getString(JobDataKeys.JOB_PARAMS);

        Instant start = Instant.now();
        boolean success = true;
        String message = "OK";
        Exception failure = null;
        try {
            quartzJobRegistry.invoke(invokeTarget, jobParams);
        } catch (Exception ex) {
            success = false;
            message = rootMessage(ex);
            failure = ex;
            log.warn("Job failed: id={}, target={}, err={}", jobId, invokeTarget, message);
        } finally {
            Instant end = Instant.now();
            if (executionRecorder != null && jobId != null && jobId > 0) {
                executionRecorder.record(new JobExecutionRecord(
                        jobId,
                        jobName,
                        invokeTarget,
                        jobParams,
                        success,
                        truncate(message, 2000),
                        start,
                        end,
                        Math.max(0, end.toEpochMilli() - start.toEpochMilli())
                ));
            }
        }
        if (failure != null) {
            throw new JobExecutionException(failure);
        }
    }

    /**
     * 提取异常链最内层的可读消息。
     *
     * @param ex 原始异常
     * @return 根因消息，无消息时返回异常类名
     */
    private static String rootMessage(Throwable ex) {
        Throwable cur = ex;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        String msg = cur.getMessage();
        return msg == null || msg.isBlank() ? cur.getClass().getSimpleName() : msg;
    }

    /**
     * 截断执行日志消息至指定长度。
     *
     * @param text 原始消息
     * @param max  最大字符数
     * @return 截断后的消息；{@code text == null} 时返回 null
     */
    private static String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
