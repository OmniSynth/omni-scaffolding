package com.omni.scaffolding.modules.system.dto.menu;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点（管理端完整树或侧栏 DIR/MENU）。
 */
@Data
public class MenuTreeNode {

    /**
     * 菜单 ID。
     */
    private Long id;

    /**
     * 父节点 ID。
     */
    private Long parentId;

    /**
     * DIR / MENU / BUTTON。
     */
    private String type;

    /**
     * 显示名称。
     */
    private String name;

    /**
     * 路由 path。
     */
    private String path;

    /**
     * 前端组件路径（可选）。
     */
    private String component;

    /**
     * 图标（可选）。
     */
    private String icon;

    /**
     * 权限码（按钮/菜单可见性）。
     */
    private String perms;

    /**
     * 排序。
     */
    private Integer sort;

    /**
     * 是否侧栏可见。
     */
    private Boolean visible;

    /**
     * 是否启用。
     */
    private Boolean status;

    /**
     * 子节点。
     */
    private List<MenuTreeNode> children = new ArrayList<>();
}
