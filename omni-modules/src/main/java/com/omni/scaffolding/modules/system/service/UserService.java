package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.cache.CacheNames;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.infra.file.FileContentSigner;
import com.omni.scaffolding.modules.system.dto.user.CreateUserRequest;
import com.omni.scaffolding.modules.system.dto.auth.ResetPasswordRequest;
import com.omni.scaffolding.modules.system.dto.user.UpdateUserRequest;
import com.omni.scaffolding.modules.system.dto.user.UserDetailView;
import com.omni.scaffolding.modules.system.dto.excel.UserExportRow;
import com.omni.scaffolding.modules.system.entity.SysRole;
import com.omni.scaffolding.modules.system.entity.SysUser;
import com.omni.scaffolding.modules.system.mapper.SysUserQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysDeptRepository;
import com.omni.scaffolding.modules.system.repository.SysPostRepository;
import com.omni.scaffolding.modules.system.repository.SysRoleRepository;
import com.omni.scaffolding.modules.system.repository.SysUserRepository;
import com.omni.scaffolding.security.SecurityUtils;
import com.omni.scaffolding.security.datascope.DataScopeQuery;
import com.omni.scaffolding.security.datascope.DataScopeType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户领域服务 —— 双轨持久化 + 数据范围 + 档案字段 / 岗位。
 *
 * <ul>
 *   <li>写路径：JPA 写 {@link SysUser}，角色 / 岗位关联走 MyBatis</li>
 *   <li>读路径：MyBatis 联查部门 / 岗位 / 角色 / 权限</li>
 *   <li>列表与写操作均按当前登录用户数据范围校验，防止越权</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> GENDERS = Set.of("UNKNOWN", "MALE", "FEMALE");

    /**
     * 导出最大行数，避免一次拉取过大。
     */
    private static final long EXPORT_LIMIT = 10_000L;

    private final SysUserRepository userRepository;
    private final SysDeptRepository deptRepository;
    private final SysRoleRepository roleRepository;
    private final SysPostRepository postRepository;
    private final SysUserQueryMapper userQueryMapper;
    private final PasswordEncoder passwordEncoder;
    private final DataScopeResolver dataScopeResolver;
    private final PermissionCacheEvictor permissionCacheEvictor;
    private final FileContentSigner fileContentSigner;

    /**
     * 创建用户（JPA 写入 + 分配角色 / 岗位）。
     *
     * @param request 创建请求
     * @return 新建用户详情
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, allEntries = true)
    public UserDetailView createUser(CreateUserRequest request) {
        assertDeptExists(request.getDeptId());
        assertRolesExist(request.getRoleIds());
        assertPostsExist(request.getPostIds());
        assertDeptInScope(request.getDeptId());
        assertUniqueContact(null, request.getMobile(), request.getEmail());

        userRepository.findByUsernameAndDeleted(request.getUsername(), 0).ifPresent(u -> {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        });

        SysUser user = new SysUser();
        user.setId(IdGenerator.nextId());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        applyProfile(user,
                request.getNickname(),
                request.getRealName(),
                request.getMobile(),
                request.getEmail(),
                request.getGender(),
                request.getAvatarFileId());
        user.setDeptId(request.getDeptId());
        user.setEnabled(request.getEnabled() == null || request.getEnabled());
        user.setDeleted(0);
        // 须 flush：随后 MyBatis 写 sys_user_role/post，未落库会触发外键失败
        userRepository.saveAndFlush(user);
        replaceRoles(user.getId(), request.getRoleIds());
        replacePosts(user.getId(), request.getPostIds());
        permissionCacheEvictor.evictUser(user.getId());
        return requireDetail(user.getId());
    }

    /**
     * 修改用户档案、部门、角色、岗位；须在数据范围内。
     *
     * @param userId  用户主键
     * @param request 修改请求
     * @return 更新后详情
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, allEntries = true)
    public UserDetailView updateUser(Long userId, UpdateUserRequest request) {
        SysUser user = userRepository.findById(userId)
                .filter(u -> u.getDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        assertInScope(user);
        assertDeptExists(request.getDeptId());
        assertRolesExist(request.getRoleIds());
        assertPostsExist(request.getPostIds());
        assertDeptInScope(request.getDeptId());
        assertUniqueContact(userId, request.getMobile(), request.getEmail());

        applyProfile(user,
                request.getNickname(),
                request.getRealName(),
                request.getMobile(),
                request.getEmail(),
                request.getGender(),
                request.getAvatarFileId());
        user.setDeptId(request.getDeptId());
        user.setEnabled(request.getEnabled());
        userRepository.saveAndFlush(user);
        replaceRoles(userId, request.getRoleIds());
        replacePosts(userId, request.getPostIds());
        permissionCacheEvictor.evictUser(userId);
        return requireDetail(userId);
    }

    /**
     * 单独切换用户启停；禁止停用管理员与当前登录用户。
     *
     * @param userId  用户主键
     * @param enabled 是否启用
     * @return 更新后详情
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, allEntries = true)
    public UserDetailView changeEnabled(Long userId, boolean enabled) {
        SysUser user = userRepository.findById(userId)
                .filter(u -> u.getDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        assertInScope(user);
        if (!enabled && userId.equals(1L)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能停用管理员账号");
        }
        if (!enabled && userId.equals(SecurityUtils.requireUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能停用当前登录用户");
        }
        user.setEnabled(enabled);
        userRepository.saveAndFlush(user);
        permissionCacheEvictor.evictUser(userId);
        return requireDetail(userId);
    }

    /**
     * 逻辑删除用户；禁止删 admin 与当前登录用户。
     *
     * @param userId 用户主键
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, allEntries = true)
    public void removeUser(Long userId) {
        SysUser user = userRepository.findById(userId)
                .filter(u -> u.getDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        assertInScope(user);
        if (userId.equals(1L)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能删除管理员账号");
        }
        if (userId.equals(SecurityUtils.requireUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能删除当前登录用户");
        }
        user.setDeleted(1);
        userRepository.saveAndFlush(user);
        userQueryMapper.deleteUserRoles(userId);
        userQueryMapper.deleteUserPosts(userId);
        permissionCacheEvictor.evictUser(userId);
    }

    /**
     * 重置密码（BCrypt）。
     *
     * @param userId  用户主键
     * @param request 新密码请求
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, allEntries = true)
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        SysUser user = userRepository.findById(userId)
                .filter(u -> u.getDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        assertInScope(user);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    /**
     * 用户详情（MyBatis 联查），结果缓存；越权范围返回 403。
     *
     * @param userId 用户主键
     * @return 详情读模型
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.USERS, key = "#userId")
    public UserDetailView getUserDetail(Long userId) {
        UserDetailView detail = userQueryMapper.findUserDetail(userId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!dataScopeResolver.canAccessUser(detail.getId(), detail.getDeptId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该用户");
        }
        fillAvatarUrl(detail);
        return detail;
    }

    /**
     * 关键字分页搜索用户（动态 SQL + 数据范围过滤）。
     *
     * @param keyword 可选，匹配用户名 / 昵称 / 姓名 / 手机 / 邮箱
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<UserDetailView> searchUsers(String keyword, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        var ds = dataScopeResolver.resolve();
        long total = userQueryMapper.countUsers(keyword, ds);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, userQueryMapper.searchUsers(keyword, ds, pq.getSize(), pq.getOffset())
                .stream().peek(this::fillAvatarUrl).toList());
    }

    /**
     * 导出用户列表（同列表过滤与数据范围，最多 {@link #EXPORT_LIMIT} 行）。
     *
     * @param keyword 可选，匹配关键字
     * @return 导出行列表
     */
    @Transactional(readOnly = true)
    public List<UserExportRow> exportUsers(String keyword) {
        var ds = dataScopeResolver.resolve();
        return userQueryMapper.searchUsers(keyword, ds, EXPORT_LIMIT, 0).stream()
                .map(this::toExportRow)
                .toList();
    }

    /**
     * 用户详情读模型转导出行（性别、状态等转中文）。
     *
     * @param view 用户详情
     * @return 导出行
     */
    private UserExportRow toExportRow(UserDetailView view) {
        UserExportRow row = new UserExportRow();
        row.setId(view.getId());
        row.setUsername(view.getUsername());
        row.setRealName(view.getRealName());
        row.setNickname(view.getNickname());
        row.setMobile(view.getMobile());
        row.setEmail(view.getEmail());
        row.setGender(genderLabel(view.getGender()));
        row.setDeptName(view.getDeptName());
        row.setPosts(joinNames(view.getPosts()));
        row.setRoles(joinNames(view.getRoles()));
        row.setEnabled(Boolean.TRUE.equals(view.getEnabled()) ? "启用" : "停用");
        return row;
    }

    /**
     * 性别枚举转中文标签。
     *
     * @param gender 性别编码
     * @return 中文标签
     */
    private static String genderLabel(String gender) {
        if (gender == null) {
            return "未知";
        }
        return switch (gender) {
            case "MALE" -> "男";
            case "FEMALE" -> "女";
            default -> "未知";
        };
    }

    /**
     * 将名称列表用顿号拼接为单个字符串。
     *
     * @param names 名称列表
     * @return 拼接结果，空列表返回空串
     */
    private static String joinNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }
        return names.stream().filter(n -> n != null && !n.isBlank()).collect(Collectors.joining("、"));
    }

    /**
     * 写入昵称、姓名、手机、邮箱、性别、头像等档案字段。
     */
    private void applyProfile(SysUser user,
                              String nickname,
                              String realName,
                              String mobile,
                              String email,
                              String gender,
                              Long avatarFileId) {
        user.setNickname(nickname);
        user.setRealName(blankToNull(realName));
        user.setMobile(blankToNull(mobile));
        user.setEmail(blankToNull(email));
        String g = blankToNull(gender);
        if (g == null) {
            g = "UNKNOWN";
        } else {
            g = g.trim().toUpperCase(Locale.ROOT);
        }
        if (!GENDERS.contains(g)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "性别取值无效");
        }
        user.setGender(g);
        user.setAvatarFileId(avatarFileId);
    }

    /**
     * 全量替换用户-角色关联。
     */
    private void replaceRoles(Long userId, List<Long> roleIds) {
        userQueryMapper.deleteUserRoles(userId);
        for (Long roleId : roleIds) {
            userQueryMapper.insertUserRole(userId, roleId);
        }
    }

    /**
     * 全量替换用户-岗位关联。
     */
    private void replacePosts(Long userId, List<Long> postIds) {
        userQueryMapper.deleteUserPosts(userId);
        if (postIds == null) {
            return;
        }
        for (Long postId : postIds) {
            userQueryMapper.insertUserPost(userId, postId);
        }
    }

    /**
     * 写后立即回读详情；读不到视为内部错误。
     */
    private UserDetailView requireDetail(Long userId) {
        UserDetailView detail = userQueryMapper.findUserDetail(userId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "用户已创建但读取失败");
        }
        fillAvatarUrl(detail);
        return detail;
    }

    private void fillAvatarUrl(UserDetailView detail) {
        if (detail == null || detail.getAvatarFileId() == null) {
            return;
        }
        long expire = fileContentSigner.defaultExpireEpoch();
        String sign = fileContentSigner.sign(detail.getAvatarFileId(), expire);
        detail.setAvatarUrl(fileContentSigner.buildContentPath(detail.getAvatarFileId(), expire, sign));
    }

    /**
     * 目标用户须在当前登录者数据范围内。
     */
    private void assertInScope(SysUser user) {
        if (!dataScopeResolver.canAccessUser(user.getId(), user.getDeptId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该用户");
        }
    }

    /**
     * 目标部门须在当前登录者数据范围内。
     */
    private void assertDeptInScope(Long deptId) {
        DataScopeQuery ds = dataScopeResolver.resolve();
        if (ds.isAll()) {
            return;
        }
        DataScopeType type = DataScopeType.from(ds.getType());
        if (type == DataScopeType.SELF || type == DataScopeType.DEPT) {
            if (!SecurityUtils.requireDeptId().equals(deptId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "无权使用该部门");
            }
            return;
        }
        if (ds.getDeptIds() == null || !ds.getDeptIds().contains(deptId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权使用该部门");
        }
    }

    /**
     * 部门存在且未删除。
     */
    private void assertDeptExists(Long deptId) {
        deptRepository.findByIdAndDeleted(deptId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "部门不存在"));
    }

    /**
     * 角色存在、未删除且已启用（停用角色不可新分配）。
     */
    private void assertRolesExist(List<Long> roleIds) {
        for (Long roleId : roleIds) {
            SysRole role = roleRepository.findByIdAndDeleted(roleId, 0)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在: " + roleId));
            if (!Boolean.TRUE.equals(role.getStatus())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "角色已停用: " + role.getName());
            }
        }
    }

    /**
     * 岗位存在且未删除。
     */
    private void assertPostsExist(List<Long> postIds) {
        if (postIds == null) {
            return;
        }
        for (Long postId : postIds) {
            postRepository.findByIdAndDeleted(postId, 0)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "岗位不存在: " + postId));
        }
    }

    /**
     * 手机号、邮箱在未删除用户中唯一（空值跳过）。
     */
    private void assertUniqueContact(Long userId, String mobile, String email) {
        String m = blankToNull(mobile);
        if (m != null) {
            userRepository.findByMobileAndDeleted(m, 0).ifPresent(other -> {
                if (userId == null || !other.getId().equals(userId)) {
                    throw new BusinessException(ErrorCode.CONFLICT, "手机号已存在");
                }
            });
        }
        String e = blankToNull(email);
        if (e != null) {
            userRepository.findByEmailAndDeleted(e, 0).ifPresent(other -> {
                if (userId == null || !other.getId().equals(userId)) {
                    throw new BusinessException(ErrorCode.CONFLICT, "邮箱已存在");
                }
            });
        }
    }

    /**
     * 空白字符串转 {@code null}。
     *
     * @param value 原始字符串
     * @return 非空白时返回 trim 后的值，否则 {@code null}
     */
    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
