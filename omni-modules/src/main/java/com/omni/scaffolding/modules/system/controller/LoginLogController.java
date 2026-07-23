package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.system.dto.log.LoginLogView;
import com.omni.scaffolding.modules.system.service.LoginLogService;
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

/**
 * 登录日志查询接口。
 *
 * <p>权限码：{@code system:loginLog:query/remove}。
 */
@Tag(name = "LoginLogs")
@RestController
@RequestMapping("/api/system/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    /**
     * 分页搜索登录日志。
     *
     * @param username 可选，匹配用户名
     * @param status   可选，SUCCESS / FAIL
     * @param ip       可选，匹配 IP
     * @param page     页码
     * @param size     每页条数
     * @return 分页结果
     */
    @Operation(summary = "分页搜索登录日志")
    @GetMapping
    @PreAuthorize("hasAuthority('system:loginLog:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<LoginLogView>> search(@RequestParam(required = false) String username,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String ip,
                                                        @RequestParam(required = false) Long page,
                                                        @RequestParam(required = false) Long size) {
        return ApiResponse.ok(loginLogService.search(username, status, ip, page, size));
    }

    /**
     * 删除登录日志。
     *
     * @param id 日志主键
     * @return 空成功响应
     */
    @Operation(summary = "删除登录日志")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:loginLog:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "登录日志", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        loginLogService.remove(id);
        return ApiResponse.ok();
    }
}
