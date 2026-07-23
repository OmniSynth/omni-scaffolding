package com.omni.scaffolding.modules.system.dto;

import lombok.Data;

import java.time.Instant;

/**
 * 定时任务执行日志读模型。
 */
@Data
public class JobLogView {

    /** 主键。 */
    private Long id;

    /** 关联任务主键。 */
    private Long jobId;

    /** 任务名称（执行快照）。 */
    private String jobName;

    /** 调用目标（执行快照）。 */
    private String invokeTarget;

    /** 任务参数（执行快照）。 */
    private String jobParams;

    /** 是否执行成功。 */
    private Boolean status;

    /** 执行结果消息。 */
    private String message;

    /** 开始时间。 */
    private Instant startTime;

    /** 结束时间。 */
    private Instant endTime;

    /** 耗时（毫秒）。 */
    private Long costMs;
}
