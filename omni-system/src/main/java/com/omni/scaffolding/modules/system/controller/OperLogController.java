package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.system.dto.OperLogView;
import com.omni.scaffolding.modules.system.service.OperLogService;
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
 * 操作日志查询接口。
 *
 * <p>权限码：{@code system:operLog:query/remove}。
 */
@Tag(name = "OperLogs")
@RestController
@RequestMapping("/api/system/oper-logs")
@RequiredArgsConstructor
public class OperLogController {

    private final OperLogService operLogService;

    /**
     * 分页搜索操作日志。
     *
     * @param username 可选，匹配操作人
     * @param module   可选，匹配模块名
     * @param status   可选，SUCCESS / FAIL
     * @param page     页码
     * @param size     每页条数
     * @return 分页结果
     */
    @Operation(summary = "分页搜索操作日志")
    @GetMapping
    @PreAuthorize("hasAuthority('system:operLog:query')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<OperLogView>> search(@RequestParam(required = false) String username,
                                                       @RequestParam(required = false) String module,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) Long page,
                                                       @RequestParam(required = false) Long size) {
        return ApiResponse.ok(operLogService.search(username, module, status, page, size));
    }

    /**
     * 删除操作日志。
     *
     * @param id 日志主键
     * @return 空成功响应
     */
    @Operation(summary = "删除操作日志")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:operLog:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "操作日志", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        operLogService.remove(id);
        return ApiResponse.ok();
    }
}
