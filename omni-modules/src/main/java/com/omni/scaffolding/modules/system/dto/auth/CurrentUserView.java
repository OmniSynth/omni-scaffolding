package com.omni.scaffolding.modules.system.dto.auth;

import com.omni.scaffolding.modules.system.dto.menu.MenuTreeNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前登录用户资料 + 侧栏菜单树（{@code GET /api/auth/me}）。
 *
 * <p>供顶栏头像、个人中心与动态路由权限使用；本人资料接口返回明文（不脱敏）。
 */
@Data
public class CurrentUserView {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 当前访问令牌 jti（用于在线用户页识别本会话）。
     */
    private String jti;

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
    private String realName;

    /**
     * 手机号。
     */
    private String mobile;

    /**
     * 邮箱。
     */
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
     * 所属部门名称。
     */
    private String deptName;

    /**
     * 岗位名称列表。
     */
    private List<String> posts = new ArrayList<>();

    /**
     * 有效数据范围。
     */
    private String dataScope;

    /**
     * 角色编码列表。
     */
    private List<String> roles = new ArrayList<>();

    /**
     * 权限编码列表。
     */
    private List<String> permissions = new ArrayList<>();

    /**
     * 是否开启动态权限（前端据此决定是否在路由切换时刷新 /me）。
     */
    private boolean dynamicPermission;

    /**
     * 侧栏菜单树（仅 DIR/MENU 且可见启用），不含 BUTTON。
     */
    private List<MenuTreeNode> menus = new ArrayList<>();
}
