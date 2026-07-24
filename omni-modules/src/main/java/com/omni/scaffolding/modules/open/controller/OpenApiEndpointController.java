package com.omni.scaffolding.modules.open.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.open.dto.endpoint.OpenEndpointSaveRequest;
import com.omni.scaffolding.modules.open.dto.endpoint.OpenEndpointView;
import com.omni.scaffolding.modules.open.service.OpenApiEndpointService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 开放接口目录管理接口（JWT）。
 *
 * <p>权限码：{@code open:endpoint:query/add/edit/remove}。
 * 列表/启用列表额外允许客户端新增编辑场景下拉选型。
 */
@Tag(name = "Open API Endpoints")
@RestController
@RequestMapping("/api/open/admin/endpoints")
@RequiredArgsConstructor
public class OpenApiEndpointController {

    private final OpenApiEndpointService endpointService;

    /**
     * 开放接口分页列表。
     *
     * @param keyword 可选，匹配编码 / 名称 / 路径
     * @param status  可选，启停过滤
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Operation(summary = "开放接口分页列表")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('open:endpoint:query','open:client:add','open:client:edit')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<OpenEndpointView>> list(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Boolean status,
                                                          @RequestParam(required = false) Long page,
                                                          @RequestParam(required = false) Long size) {
        return ApiResponse.ok(endpointService.list(keyword, status, page, size));
    }

    /**
     * 启用中的开放接口，供客户端绑定下拉使用。
     *
     * @return 启用接口列表
     */
    @Operation(summary = "启用中的开放接口（下拉）")
    @GetMapping("/enabled")
    @PreAuthorize("hasAnyAuthority('open:endpoint:query','open:client:add','open:client:edit')")
    @RateLimiter(name = "api")
    public ApiResponse<List<OpenEndpointView>> listEnabled() {
        return ApiResponse.ok(endpointService.listEnabled());
    }

    /**
     * 开放接口详情。
     *
     * @param id 接口主键
     * @return 读模型
     */
    @Operation(summary = "开放接口详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('open:endpoint:query')")
    @RateLimiter(name = "api")
    public ApiResponse<OpenEndpointView> detail(@PathVariable Long id) {
        return ApiResponse.ok(endpointService.detail(id));
    }

    /**
     * 新增开放接口。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Operation(summary = "新增开放接口")
    @PostMapping
    @PreAuthorize("hasAuthority('open:endpoint:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "开放接口目录", action = "新增")
    public ApiResponse<OpenEndpointView> create(@Valid @RequestBody OpenEndpointSaveRequest request) {
        return ApiResponse.ok(endpointService.create(request));
    }

    /**
     * 修改开放接口。
     *
     * @param id      接口主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Operation(summary = "修改开放接口")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('open:endpoint:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "开放接口目录", action = "修改")
    public ApiResponse<OpenEndpointView> update(@PathVariable Long id,
                                                @Valid @RequestBody OpenEndpointSaveRequest request) {
        return ApiResponse.ok(endpointService.update(id, request));
    }

    /**
     * 逻辑删除开放接口；仍有客户端绑定时拒绝。
     *
     * @param id 接口主键
     * @return 空成功响应
     */
    @Operation(summary = "删除开放接口")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('open:endpoint:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "开放接口目录", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        endpointService.remove(id);
        return ApiResponse.ok();
    }
}
