package com.omni.scaffolding.modules.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 定时任务执行日志。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_job_log")
public class SysJobLog {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 关联任务主键。
     */
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    /**
     * 任务名称（执行快照）。
     */
    @Column(name = "job_name", nullable = false, length = 128)
    private String jobName;

    /**
     * 调用目标（执行快照）。
     */
    @Column(name = "invoke_target", nullable = false, length = 255)
    private String invokeTarget;

    /**
     * 任务参数（执行快照）。
     */
    @Column(name = "job_params", length = 2000)
    private String jobParams;

    /**
     * 是否执行成功。
     */
    @Column(nullable = false)
    private Boolean status;

    /**
     * 执行结果消息。
     */
    @Column(length = 2000)
    private String message;

    /**
     * 开始时间。
     */
    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    /**
     * 结束时间。
     */
    @Column(name = "end_time")
    private Instant endTime;

    /**
     * 耗时（毫秒）。
     */
    @Column(name = "cost_ms")
    private Long costMs;
}
