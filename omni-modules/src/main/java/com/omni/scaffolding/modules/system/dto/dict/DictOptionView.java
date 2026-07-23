package com.omni.scaffolding.modules.system.dto.dict;

import lombok.Data;

/**
 * 字典下拉选项（业务复用）。
 */
@Data
public class DictOptionView {

    /**
     * 显示标签。
     */
    private String label;

    /**
     * 键值。
     */
    private String value;

    /**
     * 是否默认项。
     */
    private Boolean defaultFlag;

    /**
     * 样式类。
     */
    private String cssClass;
}
