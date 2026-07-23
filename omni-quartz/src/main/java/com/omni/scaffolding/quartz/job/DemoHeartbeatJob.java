package com.omni.scaffolding.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.net.InetAddress;

/**
 * 演示心跳任务：多实例部署时观察日志，确认同一 cron 只在一台执行。
 *
 * <p>{@link DisallowConcurrentExecution}：同一 Job 在本节点未完成前不会重叠调度；
 * 集群互斥仍由 JDBC JobStore 保证跨节点不双发。
 */
@Slf4j
@DisallowConcurrentExecution
public class DemoHeartbeatJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) {
        String schedulerInstanceId = "unknown";
        try {
            schedulerInstanceId = context.getScheduler().getSchedulerInstanceId();
        } catch (SchedulerException ignored) {
            // ignore
        }
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            host = "unknown-host";
        }
        log.info("Quartz demo heartbeat fired: schedulerInstanceId={}, host={}, fireTime={}",
                schedulerInstanceId, host, context.getFireTime());
    }
}
