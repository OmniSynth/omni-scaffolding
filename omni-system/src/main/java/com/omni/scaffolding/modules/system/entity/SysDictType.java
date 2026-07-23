package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典类型实体（JPA 写模型）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dict_type")
public class SysDictType extends BaseAuditableEntity {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 类型编码，全局唯一，业务引用键。
     */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    /**
     * 类型名称。
     */
    @Column(nullable = false, length = 64)
    private String name;

    /**
     * 备注。
     */
    @Column(length = 255)
    private String remark;

    /**
     * 排序。
     */
    @Column(nullable = false)
    private Integer sort = 0;

    /**
     * 是否启用。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 逻辑删除：0 正常，1 已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}
