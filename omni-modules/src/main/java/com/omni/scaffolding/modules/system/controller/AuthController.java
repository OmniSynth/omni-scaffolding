package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.common.desensitize.WithoutDesensitize;
import com.omni.scaffolding.common.util.IpUtils;
import com.omni.scaffolding.modules.system.dto.auth.ChangePasswordRequest;
import com.omni.scaffolding.modules.system.dto.auth.CurrentUserView;
import com.omni.scaffolding.modules.system.dto.auth.LoginRequest;
import com.omni.scaffolding.modules.system.dto.auth.LoginResponse;
import com.omni.scaffolding.modules.system.service.AuthService;
import com.omni.scaffolding.security.sign.LoginSignHeaders;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录换取 JWT；拉取当前用户与侧栏菜单；本人修改密码。
 *
 * <p>{@code /login} 在 Security 白名单中，需携带加签头（见 {@link LoginSignHeaders}），
 * 同时被 Resilience4j 方法级限流保护；{@code /me}、{@code /password} 需已认证。
 */
@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录并获取 JWT（需加签）。
     *
     * @param request     登录请求
     * @param httpRequest HTTP 请求，用于解析 IP 与加签头
     * @return 访问令牌与权限摘要
     */
    @Operation(summary = "登录并获取 JWT（需加签）")
    @PostMapping("/login")
    @RateLimiter(name = "api")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest) {
        String ip = IpUtils.resolveClientIp(
                httpRequest.getHeader("X-Forwarded-For"),
                httpRequest.getHeader("X-Real-IP"),
                httpRequest.getRemoteAddr());
        return ApiResponse.ok(authService.login(
                request,
                ip,
                httpRequest.getHeader("User-Agent"),
                httpRequest.getHeader(LoginSignHeaders.TIMESTAMP),
                httpRequest.getHeader(LoginSignHeaders.NONCE),
                httpRequest.getHeader(LoginSignHeaders.SIGN)));
    }

    /**
     * 当前用户资料与侧栏菜单。
     *
     * @return 个人中心读模型（明文）
     */
    @Operation(summary = "当前用户资料与侧栏菜单（本人明文）")
    @GetMapping("/me")
    @RateLimiter(name = "api")
    @WithoutDesensitize
    public ApiResponse<CurrentUserView> me() {
        return ApiResponse.ok(authService.currentUser());
    }

    /**
     * 当前用户修改密码。
     *
     * @param request 修改密码请求
     * @return 空成功响应
     */
    @Operation(summary = "当前用户修改密码")
    @PutMapping("/password")
    @RateLimiter(name = "api")
    @OperLog(module = "个人中心", action = "修改密码")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok();
    }

    /**
     * 退出登录（当前令牌失效）。
     *
     * @return 空成功响应
     */
    @Operation(summary = "退出登录（当前令牌失效）")
    @PostMapping("/logout")
    @RateLimiter(name = "api")
    @OperLog(module = "认证", action = "退出登录")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok();
    }
}

