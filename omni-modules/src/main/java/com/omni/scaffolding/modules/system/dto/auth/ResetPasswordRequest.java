package com.omni.scaffolding.modules.system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员重置用户密码请求体。
 */
@Data
public class ResetPasswordRequest {

    /**
     * 新明文密码，6~64 位；服务端 BCrypt 后落库。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度须在 6~64 之间")
    private String password;
}
