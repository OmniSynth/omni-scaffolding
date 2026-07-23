package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.cache.CacheNames;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.config.OmniSecurityProperties;
import com.omni.scaffolding.infra.file.FileContentSigner;
import com.omni.scaffolding.modules.system.dto.auth.ChangePasswordRequest;
import com.omni.scaffolding.modules.system.dto.auth.CurrentUserView;
import com.omni.scaffolding.modules.system.dto.auth.LoginRequest;
import com.omni.scaffolding.modules.system.dto.auth.LoginResponse;
import com.omni.scaffolding.modules.system.dto.menu.MenuTreeNode;
import com.omni.scaffolding.modules.system.dto.auth.UserAuthView;
import com.omni.scaffolding.modules.system.dto.user.UserDetailView;
import com.omni.scaffolding.modules.system.entity.SysUser;
import com.omni.scaffolding.modules.system.mapper.SysMenuQueryMapper;
import com.omni.scaffolding.modules.system.mapper.SysUserQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysUserRepository;
import com.omni.scaffolding.modules.system.support.TreeBuilder;
import com.omni.scaffolding.security.IssuedToken;
import com.omni.scaffolding.security.JwtService;
import com.omni.scaffolding.security.SecurityUtils;
import com.omni.scaffolding.security.datascope.DataScopeType;
import com.omni.scaffolding.security.online.OnlineSessionService;
import com.omni.scaffolding.security.sign.LoginSignService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 认证与当前用户资料服务。
 *
 * <p>登录读路径走 MyBatis 联查（用户 + 角色 + 菜单权限 + 数据范围），一次组装 JWT Claims；
 * {@code GET /api/auth/me} 返回个人中心所需资料与侧栏菜单；支持本人修改密码。
 * 登录前校验 HMAC 加签；成功/失败均写入登录日志（独立事务）。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserQueryMapper userQueryMapper;
    private final SysMenuQueryMapper menuQueryMapper;
    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OmniSecurityProperties securityProperties;
    private final PermissionCacheEvictor permissionCacheEvictor;
    private final LoginLogService loginLogService;
    private final LoginSignService loginSignService;
    private final OnlineSessionService onlineSessionService;
    private final FileContentSigner fileContentSigner;

    /**
     * 校验加签、账号密码并签发 JWT。
     *
     * <p>对外统一返回“用户名或密码错误”，防止账号枚举；失败原因写入登录日志。
     *
     * @param request   登录请求
     * @param ip        客户端 IP
     * @param userAgent User-Agent
     * @param timestamp 加签时间戳（毫秒）
     * @param nonce     加签 nonce
     * @param sign      HMAC 签名
     * @return 访问令牌与权限摘要
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request,
                               String ip,
                               String userAgent,
                               String timestamp,
                               String nonce,
                               String sign) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        try {
            loginSignService.verify(timestamp, nonce, sign, username, request.getPassword(), ip);
        } catch (BusinessException ex) {
            loginLogService.record(null, username, ip, userAgent, false, ex.getMessage());
            throw ex;
        }

        UserAuthView user = userQueryMapper.findAuthViewByUsername(username);
        if (user == null) {
            loginLogService.record(null, username, ip, userAgent, false, "用户名或密码错误");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (Boolean.FALSE.equals(user.getEnabled())) {
            loginLogService.record(user.getId(), username, ip, userAgent, false, "用户已停用");
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户已停用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginLogService.record(user.getId(), username, ip, userAgent, false, "用户名或密码错误");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 登录前失效动态权限缓存，避免 Flyway/后台改菜单后仍命中旧权限导致 403
        permissionCacheEvictor.evictUser(user.getId());
        List<String> roles = userQueryMapper.findRoleCodesByUserId(user.getId());
        List<String> permissions = userQueryMapper.findPermissionCodesByUserId(user.getId());
        String dataScope = mergeDataScope(userQueryMapper.findDataScopesByUserId(user.getId())).name();
        IssuedToken issued = jwtService.generateToken(
                user.getUsername(), user.getId(), user.getDeptId(), dataScope, roles, permissions);
        onlineSessionService.register(
                issued.jti(), user.getId(), user.getUsername(), user.getDeptId(), ip, userAgent, issued.expireAt());
        loginLogService.record(user.getId(), username, ip, userAgent, true, "登录成功");
        return new LoginResponse(
                issued.accessToken(), "Bearer", user.getId(), user.getUsername(), user.getDeptId(),
                dataScope, roles, permissions);
    }

    /**
     * 当前令牌主动登出：加入黑名单并从在线列表移除。
     */
    public void logout() {
        onlineSessionService.logout(SecurityUtils.requireAuthUser().getJti());
    }

    /**
     * 当前登录用户资料与侧栏菜单（DIR/MENU）。
     *
     * @return 个人中心读模型
     */
    @Transactional(readOnly = true)
    public CurrentUserView currentUser() {
        Long userId = SecurityUtils.requireUserId();
        UserDetailView detail = userQueryMapper.findUserDetail(userId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        boolean dynamic = securityProperties.getDynamicPermission().isEnabled();
        CurrentUserView view = new CurrentUserView();
        view.setUserId(detail.getId());
        view.setJti(SecurityUtils.requireAuthUser().getJti());
        view.setUsername(detail.getUsername());
        view.setNickname(detail.getNickname());
        view.setRealName(detail.getRealName());
        view.setMobile(detail.getMobile());
        view.setEmail(detail.getEmail());
        view.setGender(detail.getGender());
        view.setAvatarFileId(detail.getAvatarFileId());
        if (detail.getAvatarFileId() != null) {
            long expire = fileContentSigner.defaultExpireEpoch();
            String sign = fileContentSigner.sign(detail.getAvatarFileId(), expire);
            view.setAvatarUrl(fileContentSigner.buildContentPath(detail.getAvatarFileId(), expire, sign));
        }
        view.setDeptId(detail.getDeptId());
        view.setDeptName(detail.getDeptName());
        view.setPosts(detail.getPosts());
        if (dynamic) {
            view.setDataScope(mergeDataScope(userQueryMapper.findDataScopesByUserId(userId)).name());
        } else {
            view.setDataScope(SecurityUtils.requireDataScope().name());
        }
        view.setRoles(detail.getRoles());
        view.setPermissions(detail.getPermissions());
        view.setDynamicPermission(dynamic);
        List<MenuTreeNode> flat = menuQueryMapper.listSidebarMenusByUserId(userId);
        view.setMenus(TreeBuilder.buildMenuTree(flat));
        return view;
    }

    /**
     * 当前用户修改密码：校验原密码后写入 BCrypt 新哈希。
     *
     * @param request 修改密码请求
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, allEntries = true)
    public void changePassword(ChangePasswordRequest request) {
        Long userId = SecurityUtils.requireUserId();
        SysUser user = userRepository.findById(userId)
                .filter(u -> u.getDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "原密码不正确");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "新密码不能与原密码相同");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        permissionCacheEvictor.evictUser(userId);
    }

    /**
     * 多角色数据范围取最宽松：ALL &gt; DEPT_AND_CHILD &gt; DEPT &gt; SELF。
     */
    static DataScopeType mergeDataScope(List<String> scopes) {
        DataScopeType result = DataScopeType.SELF;
        if (scopes == null || scopes.isEmpty()) {
            return result;
        }
        for (String scope : scopes) {
            result = DataScopeType.max(result, DataScopeType.from(scope));
        }
        return result;
    }
}
