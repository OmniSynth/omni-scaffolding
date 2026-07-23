package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.common.excel.ExcelExportHelper;
import com.omni.scaffolding.modules.system.dto.DeptSaveRequest;
import com.omni.scaffolding.modules.system.dto.DeptView;
import com.omni.scaffolding.modules.system.dto.excel.DeptExportRow;
import com.omni.scaffolding.modules.system.service.DeptService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理接口。
 *
 * <p>权限码：{@code system:dept:query/add/edit/remove/export}。
 * 树查询按当前用户数据范围裁剪；用户表单也可凭 user 权限拉取。
 */
@Tag(name = "Departments")
@RestController
@RequestMapping("/api/system/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /**
     * 部门树（按数据范围裁剪）。
     *
     * @return 树形读模型列表
     */
    @Operation(summary = "部门树（按数据范围裁剪）")
    @GetMapping("/tree")
    @PreAuthorize("hasAnyAuthority('system:dept:query','system:user:query','system:user:add','system:user:edit')")
    @RateLimiter(name = "api")
    public ApiResponse<List<DeptView>> tree() {
        return ApiResponse.ok(deptService.tree());
    }

    /**
     * 导出部门 Excel。
     *
     * @param response HTTP 响应，直接写入文件流
     */
    @Operation(summary = "导出部门 Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:dept:export')")
    @RateLimiter(name = "api")
    @OperLog(module = "部门管理", action = "导出")
    public void export(HttpServletResponse response) {
        ExcelExportHelper.write(response, "部门数据.xlsx", DeptExportRow.class, deptService.exportDepts());
    }

    /**
     * 创建部门。
     *
     * @param request 保存请求
     * @return 新建读模型
     */
    @Operation(summary = "创建部门")
    @PostMapping
    @PreAuthorize("hasAuthority('system:dept:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "部门管理", action = "新增")
    public ApiResponse<DeptView> create(@Valid @RequestBody DeptSaveRequest request) {
        return ApiResponse.ok(deptService.create(request));
    }

    /**
     * 修改部门。
     *
     * @param id      部门主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Operation(summary = "修改部门")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "部门管理", action = "修改")
    public ApiResponse<DeptView> update(@PathVariable Long id, @Valid @RequestBody DeptSaveRequest request) {
        return ApiResponse.ok(deptService.update(id, request));
    }

    /**
     * 删除部门。
     *
     * @param id 部门主键
     * @return 空成功响应
     */
    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "部门管理", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        deptService.remove(id);
        return ApiResponse.ok();
    }
}
