package com.omni.scaffolding.modules.system.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户启用 / 停用请求。
 */
@Data
public class UserEnabledRequest {

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean enabled;
}
