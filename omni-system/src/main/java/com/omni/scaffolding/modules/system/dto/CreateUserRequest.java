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
 * 创建用户请求体。
 */
@Data
public class CreateUserRequest {

    /**
     * 登录用户名，3~64 位，全局唯一。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度须在 3~64 之间")
    private String username;

    /**
     * 初始明文密码，6~64 位；服务端会做 BCrypt。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度须在 6~64 之间")
    private String password;

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
     * 手机号（中国大陆 11 位，可空）。
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
    @Size(max = 16, message = "性别取值过长")
    private String gender = "UNKNOWN";

    /**
     * 头像文件 ID（先调统一文件上传接口拿到 id）。
     */
    private Long avatarFileId;

    /**
     * 所属部门 ID，须在当前登录用户数据范围内。
     */
    @NotNull(message = "部门不能为空")
    private Long deptId;

    /**
     * 岗位 ID 列表（可空）。
     */
    private List<Long> postIds = new ArrayList<>();

    /**
     * 角色 ID 列表，至少一个。
     */
    @NotEmpty(message = "请至少选择一个角色")
    private List<Long> roleIds;

    /**
     * 是否启用，默认 true。
     */
    private Boolean enabled = true;
}
