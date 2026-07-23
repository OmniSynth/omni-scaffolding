package com.omni.scaffolding.modules.system.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典数据实体（JPA 写模型）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dict_data")
public class SysDictData extends BaseAuditableEntity {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 所属字典类型编码。
     */
    @Column(name = "type_code", nullable = false, length = 64)
    private String typeCode;

    /**
     * 显示标签。
     */
    @Column(nullable = false, length = 128)
    private String label;

    /**
     * 存储值。
     */
    @Column(nullable = false, length = 128)
    private String value;

    /**
     * 排序。
     */
    @Column(nullable = false)
    private Integer sort = 0;

    /**
     * 前端样式类。
     */
    @Column(name = "css_class", length = 64)
    private String cssClass;

    /**
     * 是否默认项。
     */
    @Column(name = "default_flag", nullable = false)
    private Boolean defaultFlag = false;

    /**
     * 是否启用。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 备注。
     */
    @Column(length = 255)
    private String remark;

    /**
     * 逻辑删除：0 正常，1 已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}
