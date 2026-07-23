package com.omni.scaffolding.modules.system.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 修改菜单请求体。
 */
@Data
public class MenuSaveRequest {

    /**
     * 父节点 ID；根传 0。
     */
    @NotNull(message = "上级菜单不能为空")
    private Long parentId;

    /**
     * 节点类型：DIR / MENU / BUTTON。
     */
    @NotBlank(message = "菜单类型不能为空")
    private String type;

    /**
     * 显示名称。
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称长度不能超过 64")
    private String name;

    /**
     * 路由 path（BUTTON 可空）。
     */
    @Size(max = 128, message = "路由路径长度不能超过 128")
    private String path;

    /**
     * 前端组件路径（可选）。
     */
    @Size(max = 128, message = "组件路径长度不能超过 128")
    private String component;

    /**
     * 图标名（可选）。
     */
    @Size(max = 64, message = "图标长度不能超过 64")
    private String icon;

    /**
     * 权限码，如 system:user:add。
     */
    @Size(max = 128, message = "权限码长度不能超过 128")
    private String perms;

    /**
     * 同级排序。
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

    /**
     * 是否侧栏可见。
     */
    @NotNull(message = "可见状态不能为空")
    private Boolean visible;

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}
