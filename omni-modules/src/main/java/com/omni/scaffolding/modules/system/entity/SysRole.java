package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色实体（JPA）。
 *
 * <p>与用户的多对多关系表 {@code sys_user_role}、与菜单的多对多 {@code sys_role_menu} 由 Flyway 维护；
 * 数据范围挂在角色上，用户多角色时取最宽松范围。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_role")
public class SysRole extends BaseAuditableEntity {

    /**
     * 主键 ID。
     */
    @Id
    private Long id;

    /**
     * 角色编码，如 ADMIN；JWT 中 roles 使用该值，过滤器会加 ROLE_ 前缀。
     */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    /**
     * 角色显示名称。
     */
    @Column(nullable = false, length = 64)
    private String name;

    /**
     * 数据范围：{@code ALL} / {@code DEPT_AND_CHILD} / {@code DEPT} / {@code SELF}。
     */
    @Column(name = "data_scope", nullable = false, length = 32)
    private String dataScope = "SELF";

    /**
     * 是否启用；停用后不再参与登录鉴权与权限汇总。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 逻辑删除标记：0=正常，1=已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}
