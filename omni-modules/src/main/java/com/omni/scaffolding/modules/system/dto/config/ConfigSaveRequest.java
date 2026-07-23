package com.omni.scaffolding.modules.system.dto.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 修改系统参数请求。
 */
@Data
public class ConfigSaveRequest {

    /**
     * 参数键。
     */
    @NotBlank(message = "参数键不能为空")
    @Size(max = 128, message = "参数键长度不能超过 128")
    private String configKey;

    /**
     * 参数名称。
     */
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 128, message = "参数名称长度不能超过 128")
    private String configName;

    /**
     * 参数值。
     */
    @Size(max = 2000, message = "参数值长度不能超过 2000")
    private String configValue;

    /**
     * 备注。
     */
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;

    /**
     * 排序。
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status = true;
}
