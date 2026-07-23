package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色启用 / 停用请求。
 */
@Data
public class RoleStatusRequest {

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}
