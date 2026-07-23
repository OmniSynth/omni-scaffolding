package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 修改字典数据请求。
 */
@Data
public class DictDataSaveRequest {

    /**
     * 所属类型编码，必填。
     */
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 64, message = "字典类型编码长度不能超过 64")
    private String typeCode;

    /**
     * 显示标签，必填。
     */
    @NotBlank(message = "字典标签不能为空")
    @Size(max = 128, message = "字典标签长度不能超过 128")
    private String label;

    /**
     * 键值，必填。
     */
    @NotBlank(message = "字典键值不能为空")
    @Size(max = 128, message = "字典键值长度不能超过 128")
    private String value;

    /**
     * 排序，必填。
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

    /**
     * 样式类，可空。
     */
    @Size(max = 64, message = "样式类长度不能超过 64")
    private String cssClass;

    /**
     * 是否默认项，默认否。
     */
    @NotNull(message = "默认标记不能为空")
    private Boolean defaultFlag = false;

    /**
     * 是否启用，默认启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status = true;

    /**
     * 备注，可空。
     */
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;
}
