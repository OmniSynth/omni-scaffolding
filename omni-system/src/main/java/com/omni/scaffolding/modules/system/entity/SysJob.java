package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 定时任务元数据（JPA 写模型）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_job")
public class SysJob extends BaseAuditableEntity {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 任务名称，未删除范围内唯一。
     */
    @Column(name = "job_name", nullable = false, length = 128)
    private String jobName;

    /**
     * 任务分组，Quartz 调度键之一。
     */
    @Column(name = "job_group", nullable = false, length = 64)
    private String jobGroup = "omni-job";

    /**
     * 调用目标（Bean 方法表达式）。
     */
    @Column(name = "invoke_target", nullable = false, length = 255)
    private String invokeTarget;

    /**
     * 任务参数（可选 JSON）。
     */
    @Column(name = "job_params", length = 2000)
    private String jobParams;

    /**
     * Cron 表达式。
     */
    @Column(name = "cron_expression", nullable = false, length = 64)
    private String cronExpression;

    /**
     * 错失执行策略：0 忽略，1 立即触发一次，2 触发所有错过。
     */
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "misfire_policy", nullable = false)
    private Integer misfirePolicy = 0;

    /**
     * 是否允许并发执行。
     */
    @Column(nullable = false)
    private Boolean concurrent = false;

    /**
     * 是否启用。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 备注。
     */
    @Column(length = 255)
    private String remark;

    /**
     * 逻辑删除：0 正常，1 已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}
