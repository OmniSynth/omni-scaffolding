package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 菜单 / 按钮权限节点（JPA 写模型）。
 *
 * <p>类型：
 * <ul>
 *   <li>{@code DIR}：目录，侧栏分组</li>
 *   <li>{@code MENU}：菜单页，对应前端路由</li>
 *   <li>{@code BUTTON}：按钮权限，{@code perms} 作为 {@code hasAuthority} 码</li>
 * </ul>
 * 登录权限码从角色关联的菜单 {@code perms} 去重得到，不再使用扁平 {@code sys_permission}。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_menu")
public class SysMenu extends BaseAuditableEntity {

    /**
     * 主键 ID。
     */
    @Id
    private Long id;

    /**
     * 父节点 ID；根为 0。
     */
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    /**
     * 节点类型：DIR / MENU / BUTTON。
     */
    @Column(nullable = false, length = 16)
    private String type;

    /**
     * 显示名称。
     */
    @Column(nullable = false, length = 64)
    private String name;

    /**
     * 路由 path：目录多为绝对路径如 {@code /system}；菜单多为相对段如 {@code user}。
     */
    @Column(length = 128)
    private String path;

    /**
     * 前端组件路径提示（可选），如 {@code system/user/index}。
     */
    @Column(length = 128)
    private String component;

    /**
     * 图标名（可选）。
     */
    @Column(length = 64)
    private String icon;

    /**
     * 权限码，如 {@code system:user:add}；登录写入 JWT perms，按钮级鉴权用。
     */
    @Column(length = 128)
    private String perms;

    /**
     * 同级排序。
     */
    @Column(nullable = false)
    private Integer sort = 0;

    /**
     * 是否在侧栏可见（BUTTON 通常也可见标记为 true，但不参与侧栏渲染）。
     */
    @Column(nullable = false)
    private Boolean visible = true;

    /**
     * 是否启用；停用后不参与登录权限与侧栏。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 逻辑删除标记：0=正常，1=已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}
