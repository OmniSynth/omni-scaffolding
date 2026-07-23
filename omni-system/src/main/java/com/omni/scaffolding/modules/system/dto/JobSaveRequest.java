package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 修改定时任务请求。
 */
@Data
public class JobSaveRequest {

    /** 任务名称。 */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 128)
    private String jobName;

    /** 任务分组，默认 omni-job。 */
    @Size(max = 64)
    private String jobGroup;

    /** 调用目标（Bean 方法表达式）。 */
    @NotBlank(message = "调用目标不能为空")
    @Size(max = 255)
    private String invokeTarget;

    /** 任务参数（可选 JSON）。 */
    @Size(max = 2000)
    private String jobParams;

    /** Cron 表达式。 */
    @NotBlank(message = "Cron 表达式不能为空")
    @Size(max = 64)
    private String cronExpression;

    /** 错失执行策略：0 忽略，1 立即触发一次，2 触发所有错过。 */
    @NotNull
    private Integer misfirePolicy = 0;

    /** 是否允许并发执行。 */
    @NotNull
    private Boolean concurrent = false;

    /** 是否启用。 */
    @NotNull
    private Boolean status = true;

    /** 备注。 */
    @Size(max = 255)
    private String remark;
}
