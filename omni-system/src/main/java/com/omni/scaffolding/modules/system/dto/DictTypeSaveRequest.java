package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 修改字典类型请求。
 */
@Data
public class DictTypeSaveRequest {

    /**
     * 类型编码，必填。
     */
    @NotBlank(message = "字典编码不能为空")
    @Size(max = 64, message = "字典编码长度不能超过 64")
    private String code;

    /**
     * 类型名称，必填。
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 64, message = "字典名称长度不能超过 64")
    private String name;

    /**
     * 备注，可空。
     */
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;

    /**
     * 排序，必填。
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

    /**
     * 是否启用，默认启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status = true;
}
