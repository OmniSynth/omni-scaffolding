package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 当前用户修改密码请求。
 */
@Data
public class ChangePasswordRequest {

    /**
     * 原密码。
     */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /**
     * 新密码，至少 6 位。
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 64, message = "新密码长度须在 6~64 之间")
    private String newPassword;
}
