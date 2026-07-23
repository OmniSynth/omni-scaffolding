package com.omni.scaffolding.modules.system.dto.config;

import lombok.Data;

/**
 * 系统参数读模型。
 */
@Data
public class ConfigView {

    /**
     * 主键。
     */
    private Long id;

    /**
     * 参数键。
     */
    private String configKey;

    /**
     * 参数名称。
     */
    private String configName;

    /**
     * 参数值。
     */
    private String configValue;

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
     * 是否内置参数。
     */
    private Boolean builtin;
}
