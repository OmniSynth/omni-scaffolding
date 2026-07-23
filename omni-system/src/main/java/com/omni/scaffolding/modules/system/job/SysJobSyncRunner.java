package com.omni.scaffolding.modules.system.job;

import com.omni.scaffolding.modules.system.entity.SysJob;
import com.omni.scaffolding.modules.system.repository.SysJobRepository;
import com.omni.scaffolding.modules.system.service.JobService;
import com.omni.scaffolding.quartz.support.QuartzJobScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时将启用中的 {@code sys_job} 同步到 Quartz Scheduler。
 */
@Slf4j
@Component
@Order(200)
@ConditionalOnProperty(prefix = "omni.quartz", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class SysJobSyncRunner implements ApplicationRunner {

    private final SysJobRepository jobRepository;
    private final JobService jobService;
    private final QuartzJobScheduler quartzJobScheduler;

    /**
     * 启动时将启用中的 sys_job 逐条同步到 Quartz Scheduler。
     *
     * @param args 启动参数（未使用）
     */
    @Override
    public void run(ApplicationArguments args) {
        List<SysJob> jobs = jobRepository.findByDeletedAndStatus(0, true);
        int ok = 0;
        for (SysJob job : jobs) {
            try {
                quartzJobScheduler.createOrUpdate(jobService.toDefinition(job));
                ok++;
            } catch (Exception ex) {
                log.warn("同步定时任务失败: id={}, name={}, err={}", job.getId(), job.getJobName(), ex.getMessage());
            }
        }
        log.info("定时任务同步完成: enabled={}, synced={}", jobs.size(), ok);
    }
}
