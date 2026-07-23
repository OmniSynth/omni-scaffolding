package com.omni.scaffolding.modules.system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求体。
 */
@Data
public class LoginRequest {

    /**
     * 登录用户名。
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 明文密码（仅传输层出现，落库前必须哈希）。
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
