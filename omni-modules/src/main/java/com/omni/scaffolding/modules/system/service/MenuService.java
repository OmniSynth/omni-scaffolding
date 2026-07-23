package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.dto.menu.MenuSaveRequest;
import com.omni.scaffolding.modules.system.dto.menu.MenuTreeNode;
import com.omni.scaffolding.modules.system.entity.SysMenu;
import com.omni.scaffolding.modules.system.mapper.SysMenuQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysMenuRepository;
import com.omni.scaffolding.modules.system.support.TreeBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 菜单领域服务。
 *
 * <p>维护目录（DIR）/ 菜单（MENU）/ 按钮（BUTTON）树；写操作后清空动态权限缓存，
 * 因菜单 {@code perms}、状态变更会影响全站权限汇总。
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    private static final Set<String> TYPES = Set.of("DIR", "MENU", "BUTTON");

    private final SysMenuRepository menuRepository;
    private final SysMenuQueryMapper menuQueryMapper;
    private final PermissionCacheEvictor permissionCacheEvictor;

    /**
     * 返回完整菜单树（含按钮节点，供角色授权勾选）。
     *
     * @return 树形节点列表
     */
    @Transactional(readOnly = true)
    public List<MenuTreeNode> tree() {
        return TreeBuilder.buildMenuTree(menuQueryMapper.listAll());
    }

    /**
     * 新增菜单节点；父节点非根时须存在。
     *
     * @param request 保存请求
     * @return 新建节点
     */
    @Transactional
    public MenuTreeNode create(MenuSaveRequest request) {
        validate(request);
        if (request.getParentId() != null && request.getParentId() != 0L) {
            menuRepository.findByIdAndDeleted(request.getParentId(), 0)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "上级菜单不存在"));
        }
        SysMenu menu = new SysMenu();
        menu.setId(IdGenerator.nextId());
        apply(menu, request);
        menu.setDeleted(0);
        // 须 flush：随后 MyBatis 读节点，未刷盘会读不到
        menuRepository.saveAndFlush(menu);
        permissionCacheEvictor.evictAll();
        return findNode(menu.getId());
    }

    /**
     * 更新菜单；禁止将父节点设为自己。
     *
     * @param menuId  菜单主键
     * @param request 保存请求
     * @return 更新后节点
     */
    @Transactional
    public MenuTreeNode update(Long menuId, MenuSaveRequest request) {
        validate(request);
        SysMenu menu = menuRepository.findByIdAndDeleted(menuId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "菜单不存在"));
        if (menuId.equals(request.getParentId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上级菜单不能是自己");
        }
        if (request.getParentId() != null && request.getParentId() != 0L) {
            menuRepository.findByIdAndDeleted(request.getParentId(), 0)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "上级菜单不存在"));
        }
        apply(menu, request);
        menuRepository.saveAndFlush(menu);
        permissionCacheEvictor.evictAll();
        return findNode(menuId);
    }

    /**
     * 逻辑删除菜单；存在子节点时拒绝，避免悬空树。
     *
     * @param menuId 菜单主键
     */
    @Transactional
    public void remove(Long menuId) {
        SysMenu menu = menuRepository.findByIdAndDeleted(menuId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "菜单不存在"));
        if (menuRepository.countByParentIdAndDeleted(menuId, 0) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "请先删除子菜单");
        }
        menu.setDeleted(1);
        menuRepository.save(menu);
        permissionCacheEvictor.evictAll();
    }

    /**
     * 将请求字段应用到实体。
     */
    private void apply(SysMenu menu, MenuSaveRequest request) {
        menu.setParentId(request.getParentId());
        menu.setType(request.getType().trim().toUpperCase());
        menu.setName(request.getName().trim());
        menu.setPath(blankToNull(request.getPath()));
        menu.setComponent(blankToNull(request.getComponent()));
        menu.setIcon(blankToNull(request.getIcon()));
        menu.setPerms(blankToNull(request.getPerms()));
        menu.setSort(request.getSort());
        menu.setVisible(request.getVisible());
        menu.setStatus(request.getStatus());
    }

    /**
     * 校验类型为 DIR / MENU / BUTTON。
     */
    private void validate(MenuSaveRequest request) {
        String type = request.getType() == null ? "" : request.getType().trim().toUpperCase();
        if (!TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "菜单类型无效");
        }
    }

    /**
     * 从读模型列表中取回刚写入的节点。
     */
    private MenuTreeNode findNode(Long id) {
        return menuQueryMapper.listAll().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "菜单读取失败"));
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
