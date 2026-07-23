package com.omni.scaffolding.modules.system.dto.job;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Cron 表达式校验请求。
 */
@Data
public class CronValidateRequest {

    /**
     * Cron 表达式。
     */
    @NotBlank(message = "Cron 表达式不能为空")
    private String cronExpression;
}
