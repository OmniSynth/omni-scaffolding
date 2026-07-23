package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.dto.RoleSaveRequest;
import com.omni.scaffolding.modules.system.dto.RoleView;
import com.omni.scaffolding.modules.system.dto.excel.RoleExportRow;
import com.omni.scaffolding.modules.system.entity.SysRole;
import com.omni.scaffolding.modules.system.mapper.SysRoleQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysMenuRepository;
import com.omni.scaffolding.modules.system.repository.SysRoleRepository;
import com.omni.scaffolding.security.datascope.DataScopeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色领域服务。
 *
 * <p>职责：
 * <ul>
 *   <li>维护角色编码、名称、启停状态与数据范围</li>
 *   <li>维护角色-菜单（含按钮）授权关系</li>
 *   <li>写操作后失效动态权限缓存，保证开启动态权限时无需重新登录即可生效</li>
 * </ul>
 *
 * <p>约定：{@code id=1} 的 ADMIN 角色不可删除、不可停用。
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    /**
     * 内置超级管理员角色 ID，受保护。
     */
    private static final long ADMIN_ROLE_ID = 1L;

    private static final long EXPORT_LIMIT = 10_000L;

    private final SysRoleRepository roleRepository;
    private final SysMenuRepository menuRepository;
    private final SysRoleQueryMapper roleQueryMapper;
    private final PermissionCacheEvictor permissionCacheEvictor;

    /**
     * 分页列出未删除角色（含菜单 ID、用户数、状态）。
     *
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<RoleView> list(Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = roleQueryMapper.countRoles();
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, roleQueryMapper.listRoles(pq.getSize(), pq.getOffset()));
    }

    /**
     * 导出角色列表。
     *
     * @return 导出行列表
     */
    @Transactional(readOnly = true)
    public List<RoleExportRow> exportRoles() {
        return roleQueryMapper.listRoles(EXPORT_LIMIT, 0).stream()
                .map(this::toExportRow)
                .toList();
    }

    /**
     * 角色读模型转导出行（数据范围、状态转中文）。
     *
     * @param view 角色读模型
     * @return 导出行
     */
    private RoleExportRow toExportRow(RoleView view) {
        RoleExportRow row = new RoleExportRow();
        row.setId(view.getId());
        row.setCode(view.getCode());
        row.setName(view.getName());
        row.setDataScope(dataScopeLabel(view.getDataScope()));
        row.setStatus(Boolean.TRUE.equals(view.getStatus()) ? "启用" : "停用");
        row.setUserCount(view.getUserCount() == null ? 0L : view.getUserCount());
        row.setMenuCount(view.getMenuIds() == null ? 0 : view.getMenuIds().size());
        return row;
    }

    /**
     * 数据范围枚举转中文标签。
     *
     * @param dataScope 数据范围编码
     * @return 中文标签，未知编码原样返回
     */
    private static String dataScopeLabel(String dataScope) {
        if (dataScope == null) {
            return "";
        }
        return switch (dataScope) {
            case "ALL" -> "全部数据";
            case "DEPT_AND_CHILD" -> "本部门及以下";
            case "DEPT" -> "本部门";
            case "SELF" -> "仅本人";
            default -> dataScope;
        };
    }

    /**
     * 角色详情；不存在则 404。
     *
     * @param roleId 角色主键
     * @return 读模型
     */
    @Transactional(readOnly = true)
    public RoleView detail(Long roleId) {
        RoleView view = roleQueryMapper.findById(roleId);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return view;
    }

    /**
     * 创建角色并写入菜单授权；编码唯一。
     *
     * @param request 保存请求
     * @return 创建后的完整读模型
     */
    @Transactional
    public RoleView create(RoleSaveRequest request) {
        validateDataScope(request.getDataScope());
        if (roleRepository.existsByCodeAndDeleted(request.getCode(), 0)) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }
        assertMenusExist(request.getMenuIds());
        SysRole role = new SysRole();
        role.setId(IdGenerator.nextId());
        role.setCode(request.getCode().trim());
        role.setName(request.getName().trim());
        role.setDataScope(request.getDataScope().trim().toUpperCase());
        role.setStatus(Boolean.TRUE.equals(request.getStatus()));
        role.setDeleted(0);
        roleRepository.save(role);
        replaceMenus(role.getId(), request.getMenuIds());
        permissionCacheEvictor.evictAll();
        return detail(role.getId());
    }

    /**
     * 更新角色元数据与菜单授权；禁止停用 ADMIN。
     *
     * @param roleId  角色主键
     * @param request 保存请求
     * @return 更新后读模型
     */
    @Transactional
    public RoleView update(Long roleId, RoleSaveRequest request) {
        validateDataScope(request.getDataScope());
        SysRole role = roleRepository.findByIdAndDeleted(roleId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "角色不存在"));
        roleRepository.findByCodeAndDeleted(request.getCode(), 0).ifPresent(other -> {
            if (!other.getId().equals(roleId)) {
                throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
            }
        });
        assertMenusExist(request.getMenuIds());
        boolean status = Boolean.TRUE.equals(request.getStatus());
        if (roleId.equals(ADMIN_ROLE_ID) && !status) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能停用系统管理员角色");
        }
        role.setCode(request.getCode().trim());
        role.setName(request.getName().trim());
        role.setDataScope(request.getDataScope().trim().toUpperCase());
        role.setStatus(status);
        roleRepository.save(role);
        replaceMenus(roleId, request.getMenuIds());
        permissionCacheEvictor.evictAll();
        return detail(roleId);
    }

    /**
     * 单独切换角色启停；停用后该角色不再参与登录鉴权与权限汇总。
     *
     * @param roleId 角色主键
     * @param status 是否启用
     * @return 更新后读模型
     */
    @Transactional
    public RoleView changeStatus(Long roleId, boolean status) {
        if (roleId.equals(ADMIN_ROLE_ID) && !status) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能停用系统管理员角色");
        }
        SysRole role = roleRepository.findByIdAndDeleted(roleId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "角色不存在"));
        role.setStatus(status);
        roleRepository.save(role);
        permissionCacheEvictor.evictAll();
        return detail(roleId);
    }

    /**
     * 逻辑删除角色；仍有用户绑定时拒绝；同步清理角色-菜单关联。
     *
     * @param roleId 角色主键
     */
    @Transactional
    public void remove(Long roleId) {
        if (roleId.equals(ADMIN_ROLE_ID)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能删除系统管理员角色");
        }
        SysRole role = roleRepository.findByIdAndDeleted(roleId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "角色不存在"));
        if (roleQueryMapper.countUsersByRoleId(roleId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色已分配给用户，无法删除");
        }
        role.setDeleted(1);
        roleRepository.save(role);
        roleQueryMapper.deleteRoleMenus(roleId);
        permissionCacheEvictor.evictAll();
    }

    /**
     * 全量替换角色菜单授权（先删后插）。
     */
    private void replaceMenus(Long roleId, List<Long> menuIds) {
        roleQueryMapper.deleteRoleMenus(roleId);
        if (menuIds == null) {
            return;
        }
        for (Long menuId : menuIds) {
            roleQueryMapper.insertRoleMenu(roleId, menuId);
        }
    }

    /**
     * 校验菜单 ID 均存在且未删除。
     */
    private void assertMenusExist(List<Long> menuIds) {
        if (menuIds == null) {
            return;
        }
        for (Long menuId : menuIds) {
            menuRepository.findByIdAndDeleted(menuId, 0)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "菜单不存在: " + menuId));
        }
    }

    /**
     * 校验数据范围枚举合法。
     */
    private void validateDataScope(String dataScope) {
        try {
            DataScopeType.from(dataScope);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "数据范围取值无效");
        }
    }
}
