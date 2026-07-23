package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统参数实体（JPA 写模型）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_config")
public class SysConfig extends BaseAuditableEntity {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 参数键，业务引用。
     */
    @Column(name = "config_key", nullable = false, length = 128)
    private String configKey;

    /**
     * 参数名称。
     */
    @Column(name = "config_name", nullable = false, length = 128)
    private String configName;

    /**
     * 参数值。
     */
    @Column(name = "config_value", length = 2000)
    private String configValue;

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
     * 内置参数不可删除。
     */
    @Column(nullable = false)
    private Boolean builtin = false;

    /**
     * 逻辑删除：0 正常，1 已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}
