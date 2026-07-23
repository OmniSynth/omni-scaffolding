package com.omni.scaffolding.modules.system.dto.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Cron 表达式校验结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CronValidateView {

    /**
     * 表达式是否有效。
     */
    private boolean valid;

    /**
     * 校验说明。
     */
    private String message;

    /**
     * 下次触发时间预览（最多 5 次）。
     */
    private List<String> nextFireTimes = new ArrayList<>();
}
