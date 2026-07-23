package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.common.excel.ExcelExportHelper;
import com.omni.scaffolding.modules.system.dto.RoleSaveRequest;
import com.omni.scaffolding.modules.system.dto.RoleStatusRequest;
import com.omni.scaffolding.modules.system.dto.RoleView;
import com.omni.scaffolding.modules.system.dto.excel.RoleExportRow;
import com.omni.scaffolding.modules.system.service.RoleService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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

/**
 * 角色管理接口。
 *
 * <p>权限码：{@code system:role:query/add/edit/remove/export}。
 * 列表接口额外允许用户新增/编辑场景下拉选型。
 * 角色挂载数据范围与菜单（含按钮）权限。
 */
@Tag(name = "Roles")
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 角色分页列表。
     *
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    @Operation(summary = "角色分页列表")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('system:role:query','system:user:add','system:user:edit')")
    @RateLimiter(name = "api")
    public ApiResponse<PageResult<RoleView>> list(@RequestParam(required = false) Long page,
                                                  @RequestParam(required = false) Long size) {
        return ApiResponse.ok(roleService.list(page, size));
    }

    /**
     * 导出角色 Excel。
     *
     * @param response HTTP 响应，直接写入文件流
     */
    @Operation(summary = "导出角色 Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:role:export')")
    @RateLimiter(name = "api")
    @OperLog(module = "角色管理", action = "导出")
    public void export(HttpServletResponse response) {
        ExcelExportHelper.write(response, "角色数据.xlsx", RoleExportRow.class, roleService.exportRoles());
    }

    /**
     * 角色详情。
     *
     * @param id 角色主键
     * @return 读模型
     */
    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    @RateLimiter(name = "api")
    public ApiResponse<RoleView> detail(@PathVariable Long id) {
        return ApiResponse.ok(roleService.detail(id));
    }

    /**
     * 创建角色。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Operation(summary = "创建角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "角色管理", action = "新增")
    public ApiResponse<RoleView> create(@Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(roleService.create(request));
    }

    /**
     * 修改角色。
     *
     * @param id      角色主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Operation(summary = "修改角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "角色管理", action = "修改")
    public ApiResponse<RoleView> update(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(roleService.update(id, request));
    }

    /**
     * 启用 / 停用角色。
     *
     * @param id      角色主键
     * @param request 状态请求
     * @return 更新后读模型
     */
    @Operation(summary = "启用/停用角色")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:role:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "角色管理", action = "变更状态")
    public ApiResponse<RoleView> changeStatus(@PathVariable Long id, @Valid @RequestBody RoleStatusRequest request) {
        return ApiResponse.ok(roleService.changeStatus(id, Boolean.TRUE.equals(request.getStatus())));
    }

    /**
     * 删除角色。
     *
     * @param id 角色主键
     * @return 空成功响应
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "角色管理", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        roleService.remove(id);
        return ApiResponse.ok();
    }
}

