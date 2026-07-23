package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 菜单 JPA Repository。
 *
 * <p>负责菜单主表写入与父子关系校验；完整树与侧栏菜单组装走 MyBatis。
 */
public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    /**
     * 按主键查询指定删除标记的菜单。
     *
     * @param id      菜单 ID
     * @param deleted 删除标记，业务侧通常传 {@code 0}
     */
    Optional<SysMenu> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 列出某父节点下的直接子菜单（含按钮）。
     *
     * @param parentId 父菜单 ID，根为 {@code 0}
     * @param deleted  删除标记
     */
    List<SysMenu> findByParentIdAndDeleted(Long parentId, Integer deleted);

    /**
     * 统计某父节点下未删除子节点数量（删除前校验是否有子菜单）。
     *
     * @param parentId 父菜单 ID
     * @param deleted  删除标记
     * @return 子节点数量
     */
    long countByParentIdAndDeleted(Long parentId, Integer deleted);
}
