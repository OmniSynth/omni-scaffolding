package com.omni.scaffolding.modules.system.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.audit.OperLog;
import com.omni.scaffolding.modules.system.dto.MenuSaveRequest;
import com.omni.scaffolding.modules.system.dto.MenuTreeNode;
import com.omni.scaffolding.modules.system.service.MenuService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理接口（含按钮节点）。
 *
 * <p>权限码：{@code system:menu:query/add/edit/remove}。
 * 角色分配菜单时也可凭 role:add/edit 拉取完整树。
 */
@Tag(name = "Menus")
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 菜单树（含按钮）。
     *
     * @return 树形节点列表
     */
    @Operation(summary = "菜单树（含按钮）")
    @GetMapping("/tree")
    @PreAuthorize("hasAnyAuthority('system:menu:query','system:role:add','system:role:edit')")
    @RateLimiter(name = "api")
    public ApiResponse<List<MenuTreeNode>> tree() {
        return ApiResponse.ok(menuService.tree());
    }

    /**
     * 创建菜单。
     *
     * @param request 保存请求
     * @return 新建节点
     */
    @Operation(summary = "创建菜单")
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    @RateLimiter(name = "api")
    @OperLog(module = "菜单管理", action = "新增")
    public ApiResponse<MenuTreeNode> create(@Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.ok(menuService.create(request));
    }

    /**
     * 修改菜单。
     *
     * @param id      菜单主键
     * @param request 保存请求
     * @return 更新后节点
     */
    @Operation(summary = "修改菜单")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @RateLimiter(name = "api")
    @OperLog(module = "菜单管理", action = "修改")
    public ApiResponse<MenuTreeNode> update(@PathVariable Long id, @Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.ok(menuService.update(id, request));
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单主键
     * @return 空成功响应
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    @RateLimiter(name = "api")
    @OperLog(module = "菜单管理", action = "删除")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        menuService.remove(id);
        return ApiResponse.ok();
    }
}
