package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 修改岗位请求体。
 */
@Data
public class PostSaveRequest {

    /**
     * 岗位编码，全局唯一。
     */
    @NotBlank(message = "岗位编码不能为空")
    @Size(max = 64, message = "岗位编码长度不能超过 64")
    private String code;

    /**
     * 岗位名称。
     */
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 64, message = "岗位名称长度不能超过 64")
    private String name;

    /**
     * 排序。
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}
