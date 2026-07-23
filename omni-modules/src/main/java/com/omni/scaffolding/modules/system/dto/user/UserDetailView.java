package com.omni.scaffolding.modules.system.dto.user;

import com.omni.scaffolding.common.desensitize.Desensitize;
import com.omni.scaffolding.common.desensitize.DesensitizeType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户读模型：含部门、岗位、角色、权限，由 MyBatis 组装，非 JPA Entity。
 *
 * <p>姓名/手机/邮箱在 JSON 输出时默认脱敏；详情编辑接口可通过 {@code @WithoutDesensitize} 返回明文。
 */
@Data
public class UserDetailView {

    /**
     * 用户 ID。
     */
    private Long id;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 显示昵称。
     */
    private String nickname;

    /**
     * 真实姓名。
     */
    @Desensitize(type = DesensitizeType.NAME)
    private String realName;

    /**
     * 手机号。
     */
    @Desensitize(type = DesensitizeType.MOBILE)
    private String mobile;

    /**
     * 邮箱。
     */
    @Desensitize(type = DesensitizeType.EMAIL)
    private String email;

    /**
     * 性别：UNKNOWN / MALE / FEMALE。
     */
    private String gender;

    /**
     * 头像文件 ID。
     */
    private Long avatarFileId;

    /**
     * 短时头像预览 URL（服务端签发，可直接用于 img src）。
     */
    private String avatarUrl;

    /**
     * 所属部门 ID。
     */
    private Long deptId;

    /**
     * 所属部门名称（联查）。
     */
    private String deptName;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 岗位 ID 列表。
     */
    private List<Long> postIds = new ArrayList<>();

    /**
     * 岗位名称列表。
     */
    private List<String> posts = new ArrayList<>();

    /**
     * 角色 ID 列表。
     */
    private List<Long> roleIds = new ArrayList<>();

    /**
     * 角色名称列表（展示用）。
     */
    private List<String> roles = new ArrayList<>();

    /**
     * 权限编码列表。
     */
    private List<String> permissions = new ArrayList<>();
}
