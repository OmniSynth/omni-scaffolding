package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 修改部门请求体。
 */
@Data
public class DeptSaveRequest {

    /**
     * 父部门 ID；根部门传 0。
     */
    @NotNull(message = "上级部门不能为空")
    private Long parentId;

    /**
     * 部门名称。
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称长度不能超过 64")
    private String name;

    /**
     * 同级排序。
     */
    @NotNull(message = "排序不能为空")
    private Integer sort;

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}
