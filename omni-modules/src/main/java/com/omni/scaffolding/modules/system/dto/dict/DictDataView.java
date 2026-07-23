package com.omni.scaffolding.modules.system.dto.dict;

import lombok.Data;

/**
 * 字典数据读模型（管理端列表 / 详情）。
 */
@Data
public class DictDataView {

    /**
     * 主键。
     */
    private Long id;

    /**
     * 所属类型编码。
     */
    private String typeCode;

    /**
     * 显示标签。
     */
    private String label;

    /**
     * 键值。
     */
    private String value;

    /**
     * 排序。
     */
    private Integer sort;

    /**
     * 样式类。
     */
    private String cssClass;

    /**
     * 是否默认项。
     */
    private Boolean defaultFlag;

    /**
     * 是否启用。
     */
    private Boolean status;

    /**
     * 备注。
     */
    private String remark;
}
