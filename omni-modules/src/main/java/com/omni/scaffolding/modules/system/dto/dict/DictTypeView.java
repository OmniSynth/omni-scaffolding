package com.omni.scaffolding.modules.system.dto.dict;

import lombok.Data;

/**
 * 字典类型读模型（管理端列表 / 详情）。
 */
@Data
public class DictTypeView {

    /**
     * 主键。
     */
    private Long id;

    /**
     * 类型编码。
     */
    private String code;

    /**
     * 类型名称。
     */
    private String name;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 排序。
     */
    private Integer sort;

    /**
     * 是否启用。
     */
    private Boolean status;

    /**
     * 该类型下未删除的数据条数。
     */
    private Long dataCount;
}
