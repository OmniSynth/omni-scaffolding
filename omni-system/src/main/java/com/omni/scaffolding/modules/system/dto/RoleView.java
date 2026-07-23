package com.omni.scaffolding.modules.system.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色读模型（含已分配菜单 ID）。
 */
@Data
public class RoleView {

    /**
     * 角色 ID。
     */
    private Long id;

    /**
     * 角色编码，如 ADMIN。
     */
    private String code;

    /**
     * 显示名称。
     */
    private String name;

    /**
     * 数据范围：ALL / DEPT_AND_CHILD / DEPT / SELF。
     */
    private String dataScope;

    /**
     * 是否启用。
     */
    private Boolean status;

    /**
     * 已绑定该角色的用户数（未删除用户）。
     */
    private Long userCount;

    /**
     * 已分配菜单（含按钮）ID 列表。
     */
    private List<Long> menuIds = new ArrayList<>();
}
