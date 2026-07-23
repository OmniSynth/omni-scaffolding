package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.system.dto.user.OnlineUserView;
import com.omni.scaffolding.modules.system.service.OnlineUserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 在线用户：列表与强制下线。
 *
 * <p>权限码：{@code system:online:query} / {@code system:online:kick}。
 */
@Tag(name = "Online Users")
@RestController
@RequestMapping("/api/system/online-users")
@RequiredArgsConstructor
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    /**
     * 在线用户列表。
     *
     * @param username 可选，模糊匹配用户名
     * @param ip       可选，模糊匹配 IP
     * @return 在线会话列表
     */
    @Operation(summary = "在线用户列表")
    @GetMapping
    @PreAuthorize("hasAuthority('system:online:query')")
    @RateLimiter(name = "api")
    public ApiResponse<List<OnlineUserView>> list(@RequestParam(required = false) String username,
                                                  @RequestParam(required = false) String ip) {
        return ApiResponse.ok(onlineUserService.list(username, ip));
    }

    /**
     * 强制下线。
     *
     * @param jti 会话 jti
     * @return 空成功响应
     */
    @Operation(summary = "强制下线")
    @DeleteMapping("/{jti}")
    @PreAuthorize("hasAuthority('system:online:kick')")
    @RateLimiter(name = "api")
    @OperLog(module = "在线用户", action = "强制下线")
    public ApiResponse<Void> kick(@PathVariable String jti) {
        onlineUserService.kick(jti);
        return ApiResponse.ok();
    }
}
