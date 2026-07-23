package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 岗位实体（JPA 写模型）。
 *
 * <p>与用户多对多关系表 {@code sys_user_post} 由 Flyway 维护。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_post")
public class SysPost extends BaseAuditableEntity {

    /**
     * 主键 ID。
     */
    @Id
    private Long id;

    /**
     * 岗位编码，全局唯一，如 ENGINEER。
     */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    /**
     * 岗位显示名称。
     */
    @Column(nullable = false, length = 64)
    private String name;

    /**
     * 排序，越小越靠前。
     */
    @Column(nullable = false)
    private Integer sort = 0;

    /**
     * 是否启用。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 逻辑删除标记：0=正常，1=已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}
