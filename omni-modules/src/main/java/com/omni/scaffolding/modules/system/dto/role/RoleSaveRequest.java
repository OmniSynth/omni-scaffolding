package com.omni.scaffolding.modules.system.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建 / 修改角色请求体。
 */
@Data
public class RoleSaveRequest {

    /**
     * 角色编码，全局唯一。
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 64, message = "角色编码长度不能超过 64")
    private String code;

    /**
     * 显示名称。
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称长度不能超过 64")
    private String name;

    /**
     * 数据范围：ALL / DEPT_AND_CHILD / DEPT / SELF。
     */
    @NotBlank(message = "数据范围不能为空")
    private String dataScope;

    /**
     * 是否启用，默认 true。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status = true;

    /**
     * 菜单 ID 列表（含半选父节点，前端一并提交）。
     */
    @NotNull(message = "菜单不能为空")
    private List<Long> menuIds = new ArrayList<>();
}
