package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysDept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 部门 JPA Repository。
 *
 * <p>负责部门主表写入与父子关系校验；部门树、用户数聚合及数据范围展开走 MyBatis。
 */
public interface SysDeptRepository extends JpaRepository<SysDept, Long> {

    /**
     * 按主键查询指定删除标记的部门。
     *
     * @param id      部门 ID
     * @param deleted 删除标记，业务侧通常传 {@code 0}
     */
    Optional<SysDept> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 列出某父部门下的直接子部门。
     *
     * @param parentId 父部门 ID，根为 {@code 0}
     * @param deleted  删除标记
     */
    List<SysDept> findByParentIdAndDeleted(Long parentId, Integer deleted);

    /**
     * 统计某父部门下未删除子节点数量（删除前校验是否有子部门）。
     *
     * @param parentId 父部门 ID
     * @param deleted  删除标记
     * @return 子节点数量
     */
    long countByParentIdAndDeleted(Long parentId, Integer deleted);
}
