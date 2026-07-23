package com.omni.scaffolding.modules.ops.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.ops.dto.RedisDeleteKeysRequest;
import com.omni.scaffolding.modules.ops.dto.RedisExpireRequest;
import com.omni.scaffolding.modules.ops.dto.RedisInfoView;
import com.omni.scaffolding.modules.ops.dto.RedisKeyDetailView;
import com.omni.scaffolding.modules.ops.dto.RedisKeyView;
import com.omni.scaffolding.modules.ops.dto.RedisSetStringRequest;
import com.omni.scaffolding.modules.ops.service.RedisOpsService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Redis 运维接口。
 *
 * <p>权限：{@code ops:redis:query/edit/remove}。不含 FLUSHDB。
 */
@Tag(name = "Ops Redis")
@RestController
@RequestMapping("/api/ops/redis")
@RequiredArgsConstructor
@Profile("!test")
public class RedisOpsController {

    private final RedisOpsService redisOpsService;

    /**
     * Redis 运行概览。
     *
     * @return 版本、内存、连接数等
     */
    @Operation(summary = "Redis 运行概览")
    @GetMapping("/info")
    @PreAuthorize("hasAuthority('ops:redis:query')")
    @RateLimiter(name = "api")
    public ApiResponse<RedisInfoView> info() {
        return ApiResponse.ok(redisOpsService.info());
    }

    /**
     * SCAN 浏览 Key。
     *
     * @param pattern 可选，匹配模式，默认 {@code *}
     * @param limit   可选，返回条数上限
     * @return Key 列表
     */
    @Operation(summary = "SCAN 浏览 Key")
    @GetMapping("/keys")
    @PreAuthorize("hasAuthority('ops:redis:query')")
    @RateLimiter(name = "api")
    public ApiResponse<List<RedisKeyView>> keys(@RequestParam(required = false) String pattern,
                                                @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(redisOpsService.scanKeys(pattern, limit));
    }

    /**
     * Key 详情。
     *
     * @param key Key 名称
     * @return 类型、TTL、值（大 value 截断）
     */
    @Operation(summary = "Key 详情")
    @GetMapping("/key")
    @PreAuthorize("hasAuthority('ops:redis:query')")
    @RateLimiter(name = "api")
    public ApiResponse<RedisKeyDetailView> detail(@RequestParam String key) {
        return ApiResponse.ok(redisOpsService.detail(key));
    }

    /**
     * 写入 String Key。
     *
     * @param request 写入请求
     * @return 更新后的 Key 详情
     */
    @Operation(summary = "写入 String Key")
    @PostMapping("/string")
    @PreAuthorize("hasAuthority('ops:redis:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "Redis运维", action = "写入String")
    public ApiResponse<RedisKeyDetailView> setString(@Valid @RequestBody RedisSetStringRequest request) {
        return ApiResponse.ok(redisOpsService.setString(request));
    }

    /**
     * 设置 / 清除 TTL。
     *
     * @param request 过期请求
     * @return Key 摘要
     */
    @Operation(summary = "设置 / 清除 TTL")
    @PutMapping("/expire")
    @PreAuthorize("hasAuthority('ops:redis:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "Redis运维", action = "设置TTL")
    public ApiResponse<RedisKeyView> expire(@Valid @RequestBody RedisExpireRequest request) {
        return ApiResponse.ok(redisOpsService.expire(request));
    }

    /**
     * 批量删除 Key。
     *
     * @param request 待删除 Key 列表
     * @return 实际删除数量
     */
    @Operation(summary = "批量删除 Key")
    @DeleteMapping("/keys")
    @PreAuthorize("hasAuthority('ops:redis:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "Redis运维", action = "删除Key")
    public ApiResponse<Map<String, Long>> deleteKeys(@Valid @RequestBody RedisDeleteKeysRequest request) {
        return ApiResponse.ok(Map.of("deleted", redisOpsService.deleteKeys(request.getKeys())));
    }
}
