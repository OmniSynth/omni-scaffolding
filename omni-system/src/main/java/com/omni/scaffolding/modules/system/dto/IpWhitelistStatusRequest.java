package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IP 白名单启停状态变更请求体。
 */
@Data
public class IpWhitelistStatusRequest {

    /**
     * 目标状态：{@code true} 启用，{@code false} 停用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}
