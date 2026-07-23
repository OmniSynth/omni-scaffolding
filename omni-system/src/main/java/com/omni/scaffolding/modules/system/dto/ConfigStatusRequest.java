package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 系统参数启停请求。
 */
@Data
public class ConfigStatusRequest {

    /** 是否启用。 */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}
