package com.omni.scaffolding.modules.system.dto;

import lombok.Data;

import java.time.Instant;

/**
 * 定时任务读模型（管理端列表 / 详情）。
 */
@Data
public class JobView {

    /**
     * 主键。
     */
    private Long id;

    /**
     * 任务名称。
     */
    private String jobName;

    /**
     * 任务分组。
     */
    private String jobGroup;

    /**
     * 调用目标。
     */
    private String invokeTarget;

    /**
     * 任务参数。
     */
    private String jobParams;

    /**
     * Cron 表达式。
     */
    private String cronExpression;

    /**
     * 错失执行策略：0 忽略，1 立即触发一次，2 触发所有错过。
     */
    private Integer misfirePolicy;

    /**
     * 是否允许并发执行。
     */
    private Boolean concurrent;

    /**
     * 是否启用。
     */
    private Boolean status;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 创建时间。
     */
    private Instant createdAt;

    /**
     * 最后更新时间。
     */
    private Instant updatedAt;
}
