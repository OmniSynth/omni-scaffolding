package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.dto.job.CronValidateView;
import com.omni.scaffolding.modules.system.dto.job.JobLogView;
import com.omni.scaffolding.modules.system.dto.job.JobSaveRequest;
import com.omni.scaffolding.modules.system.dto.job.JobView;
import com.omni.scaffolding.modules.system.entity.SysJob;
import com.omni.scaffolding.modules.system.mapper.SysJobQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysJobLogRepository;
import com.omni.scaffolding.modules.system.repository.SysJobRepository;
import com.omni.scaffolding.quartz.support.ManagedJobDefinition;
import com.omni.scaffolding.quartz.support.QuartzJobRegistry;
import com.omni.scaffolding.quartz.support.QuartzJobScheduler;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 定时任务管理：元数据 CRUD + 与 Quartz Scheduler 同步。
 */
@Service
@RequiredArgsConstructor
public class JobService {

    private final SysJobRepository jobRepository;
    private final SysJobLogRepository jobLogRepository;
    private final SysJobQueryMapper jobQueryMapper;
    private final ObjectProvider<QuartzJobScheduler> quartzJobScheduler;
    private final QuartzJobRegistry quartzJobRegistry;

    /**
     * 分页查询定时任务。
     *
     * @param keyword 可选，匹配任务名称 / 调用目标
     * @param status  可选，启停状态
     * @param page    页码，从 1 开始
     * @param size    每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<JobView> list(String keyword, Boolean status, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = jobQueryMapper.countJobs(keyword, status);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, jobQueryMapper.listJobs(keyword, status, pq.getSize(), pq.getOffset()));
    }

    /**
     * 任务详情；不存在则 404。
     *
     * @param jobId 任务主键
     * @return 读模型
     */
    @Transactional(readOnly = true)
    public JobView detail(Long jobId) {
        JobView view = jobQueryMapper.findById(jobId);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "定时任务不存在");
        }
        return view;
    }

    /**
     * 新增任务并同步至 Quartz 调度器。
     *
     * @param request 创建请求
     * @return 新建任务读模型
     */
    @Transactional
    public JobView create(JobSaveRequest request) {
        validateRequest(request);
        String name = request.getJobName().trim();
        if (jobRepository.existsByJobNameAndDeleted(name, 0)) {
            throw new BusinessException(ErrorCode.CONFLICT, "任务名称已存在");
        }
        SysJob job = new SysJob();
        job.setId(IdGenerator.nextId());
        job.setDeleted(0);
        applyMutable(job, request);
        // flush 后再走 MyBatis 读详情，避免同事务内读不到未刷盘数据
        jobRepository.saveAndFlush(job);
        syncCreateOrUpdate(job);
        return detail(job.getId());
    }

    /**
     * 修改任务并同步至 Quartz 调度器。
     *
     * @param jobId   任务主键
     * @param request 修改请求
     * @return 更新后的读模型
     */
    @Transactional
    public JobView update(Long jobId, JobSaveRequest request) {
        validateRequest(request);
        SysJob job = requireJob(jobId);
        String name = request.getJobName().trim();
        if (jobRepository.existsByJobNameAndDeletedAndIdNot(name, 0, jobId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "任务名称已存在");
        }
        applyMutable(job, request);
        jobRepository.saveAndFlush(job);
        syncCreateOrUpdate(job);
        return detail(jobId);
    }

    /**
     * 切换任务启停并同步调度状态。
     *
     * @param jobId   任务主键
     * @param enabled 是否启用
     * @return 更新后的读模型
     */
    @Transactional
    public JobView changeStatus(Long jobId, boolean enabled) {
        SysJob job = requireJob(jobId);
        job.setStatus(enabled);
        jobRepository.saveAndFlush(job);
        try {
            requireScheduler().createOrUpdate(toDefinition(job));
        } catch (SchedulerException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "调度状态同步失败: " + ex.getMessage());
        }
        return detail(jobId);
    }

    /**
     * 逻辑删除任务并从 Quartz 移除调度。
     *
     * @param jobId 任务主键
     */
    @Transactional
    public void remove(Long jobId) {
        SysJob job = requireJob(jobId);
        job.setDeleted(1);
        job.setStatus(false);
        jobRepository.save(job);
        QuartzJobScheduler scheduler = quartzJobScheduler.getIfAvailable();
        if (scheduler != null) {
            try {
                scheduler.delete(job.getId(), job.getJobGroup());
            } catch (SchedulerException ex) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "删除调度失败: " + ex.getMessage());
            }
        }
    }

    /**
     * 立即触发一次。不可使用 readOnly：Quartz JDBC 集群抢行锁需要写事务。
     *
     * @param jobId 任务主键
     */
    @Transactional
    public void runOnce(Long jobId) {
        SysJob job = requireJob(jobId);
        try {
            requireScheduler().triggerOnce(toDefinition(job));
        } catch (SchedulerException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "立即执行失败: " + ex.getMessage());
        }
    }

    /**
     * 校验 Cron 表达式并预览下次触发时间。
     *
     * @param cronExpression Cron 表达式
     * @return 校验结果
     */
    @Transactional(readOnly = true)
    public CronValidateView validateCron(String cronExpression) {
        String cron = cronExpression == null ? "" : cronExpression.trim();
        boolean valid = QuartzJobScheduler.isValidCron(cron);
        if (!valid) {
            return new CronValidateView(false, "Cron 表达式无效", List.of());
        }
        return new CronValidateView(true, "OK", QuartzJobScheduler.nextFireTimes(cron, 5));
    }

    /**
     * 分页查询任务执行日志。
     *
     * @param jobId 任务主键
     * @param page  页码，从 1 开始
     * @param size  每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<JobLogView> listLogs(Long jobId, Long page, Long size) {
        requireJob(jobId);
        PageQuery pq = PageQuery.of(page, size);
        long total = jobQueryMapper.countLogs(jobId);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, jobQueryMapper.listLogs(jobId, pq.getSize(), pq.getOffset()));
    }

    /**
     * 清空指定任务的全部执行日志。
     *
     * @param jobId 任务主键
     */
    @Transactional
    public void clearLogs(Long jobId) {
        requireJob(jobId);
        jobLogRepository.deleteByJobId(jobId);
    }

    /**
     * 将 JPA 实体转为 Quartz 调度定义。
     *
     * @param job 任务实体
     * @return 调度定义
     */
    public ManagedJobDefinition toDefinition(SysJob job) {
        return new ManagedJobDefinition(
                job.getId(),
                job.getJobName(),
                job.getJobGroup(),
                job.getInvokeTarget(),
                job.getJobParams(),
                job.getCronExpression(),
                job.getMisfirePolicy() == null ? 0 : job.getMisfirePolicy(),
                Boolean.TRUE.equals(job.getConcurrent()),
                Boolean.TRUE.equals(job.getStatus())
        );
    }

    /**
     * 校验调用目标、Cron 表达式与 misfire 策略合法。
     *
     * @param request 保存请求
     */
    private void validateRequest(JobSaveRequest request) {
        try {
            quartzJobRegistry.validateTarget(request.getInvokeTarget());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, ex.getMessage());
        }
        if (!QuartzJobScheduler.isValidCron(request.getCronExpression())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cron 表达式无效");
        }
        int policy = request.getMisfirePolicy() == null ? 0 : request.getMisfirePolicy();
        if (policy < 0 || policy > 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "misfirePolicy 仅支持 0/1/2");
        }
    }

    /**
     * 将请求中的可变字段写入定时任务实体。
     *
     * @param job     目标实体
     * @param request 保存请求
     */
    private void applyMutable(SysJob job, JobSaveRequest request) {
        job.setJobName(request.getJobName().trim());
        job.setJobGroup(StringUtils.hasText(request.getJobGroup())
                ? request.getJobGroup().trim()
                : QuartzJobScheduler.DEFAULT_GROUP);
        job.setInvokeTarget(request.getInvokeTarget().trim());
        job.setJobParams(StringUtils.hasText(request.getJobParams()) ? request.getJobParams().trim() : null);
        job.setCronExpression(request.getCronExpression().trim());
        job.setMisfirePolicy(request.getMisfirePolicy() == null ? 0 : request.getMisfirePolicy());
        job.setConcurrent(Boolean.TRUE.equals(request.getConcurrent()));
        job.setStatus(Boolean.TRUE.equals(request.getStatus()));
        job.setRemark(request.getRemark());
    }

    /**
     * 按主键加载定时任务；不存在则 404。
     *
     * @param jobId 任务主键
     * @return 未删除的任务实体
     */
    private SysJob requireJob(Long jobId) {
        return jobRepository.findByIdAndDeleted(jobId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "定时任务不存在"));
    }

    /**
     * 将任务定义同步到 Quartz 调度器（调度器不可用时跳过）。
     *
     * @param job 任务实体
     */
    private void syncCreateOrUpdate(SysJob job) {
        QuartzJobScheduler scheduler = quartzJobScheduler.getIfAvailable();
        if (scheduler == null) {
            return;
        }
        try {
            scheduler.createOrUpdate(toDefinition(job));
        } catch (SchedulerException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "调度同步失败: " + ex.getMessage());
        }
    }

    /**
     * 获取可用的 Quartz 调度器；未启用或未启动时抛业务异常。
     *
     * @return Quartz 调度器实例
     */
    private QuartzJobScheduler requireScheduler() {
        QuartzJobScheduler scheduler = quartzJobScheduler.getIfAvailable();
        if (scheduler == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Quartz 调度器不可用，请确认 omni.quartz.enabled=true 且已成功启动 Scheduler");
        }
        return scheduler;
    }
}

