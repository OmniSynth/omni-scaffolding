package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 修改用户请求体（不含用户名与密码；密码走独立重置接口）。
 */
@Data
public class UpdateUserRequest {

    /**
     * 显示昵称。
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过 64")
    private String nickname;

    /**
     * 真实姓名。
     */
    @Size(max = 64, message = "真实姓名长度不能超过 64")
    private String realName;

    /**
     * 手机号（可空）。
     */
    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 邮箱（可空）。
     */
    @Pattern(regexp = "^$|^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$", message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过 128")
    private String email;

    /**
     * 性别：UNKNOWN / MALE / FEMALE。
     */
    @NotBlank(message = "性别不能为空")
    @Size(max = 16, message = "性别取值过长")
    private String gender;

    /**
     * 头像路径。
     */
    @Size(max = 512, message = "头像路径过长")
    private String avatar;

    /**
     * 所属部门 ID。
     */
    @NotNull(message = "部门不能为空")
    private Long deptId;

    /**
     * 岗位 ID 列表。
     */
    private List<Long> postIds = new ArrayList<>();

    /**
     * 角色 ID 列表。
     */
    @NotEmpty(message = "请至少选择一个角色")
    private List<Long> roleIds;

    /**
     * 是否启用。
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
}
