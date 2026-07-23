package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 公告启停请求。
 */
@Data
public class NoticeStatusRequest {

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}
