package com.omni.scaffolding.modules.system.job;

import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.entity.SysJobLog;
import com.omni.scaffolding.modules.system.repository.SysJobLogRepository;
import com.omni.scaffolding.quartz.support.JobExecutionRecord;
import com.omni.scaffolding.quartz.support.JobExecutionRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 将 Quartz 执行结果写入 {@code sys_job_log}（独立事务，避免被业务回滚）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbJobExecutionRecorder implements JobExecutionRecorder {

    private final SysJobLogRepository jobLogRepository;

    /**
     * 将执行结果写入 {@code sys_job_log}；失败仅打 warn，不影响任务本身。
     *
     * @param record Quartz 执行摘要
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(JobExecutionRecord record) {
        try {
            SysJobLog logEntity = new SysJobLog();
            logEntity.setId(IdGenerator.nextId());
            logEntity.setJobId(record.jobId());
            logEntity.setJobName(record.jobName());
            logEntity.setInvokeTarget(record.invokeTarget());
            logEntity.setJobParams(record.jobParams());
            logEntity.setStatus(record.success());
            logEntity.setMessage(record.message());
            logEntity.setStartTime(record.startTime());
            logEntity.setEndTime(record.endTime());
            logEntity.setCostMs(record.costMs());
            jobLogRepository.save(logEntity);
        } catch (Exception ex) {
            log.warn("写入任务日志失败: jobId={}, err={}", record.jobId(), ex.getMessage());
        }
    }
}
