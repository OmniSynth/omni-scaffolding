package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 岗位 JPA Repository。
 *
 * <p>负责岗位主表简单写路径与按编码唯一性校验；列表/详情聚合查询走 MyBatis。
 * 查询方法均带 {@code deleted} 条件，与逻辑删除约定一致。
 */
public interface SysPostRepository extends JpaRepository<SysPost, Long> {

    /**
     * 按主键查询未删除（或指定删除标记）的岗位。
     *
     * @param id      岗位 ID
     * @param deleted 删除标记，业务侧通常传 {@code 0}
     */
    Optional<SysPost> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 按岗位编码查询（用于更新时判重）。
     *
     * @param code    岗位编码
     * @param deleted 删除标记
     * @return 岗位实体
     */
    Optional<SysPost> findByCodeAndDeleted(String code, Integer deleted);

    /**
     * 编码是否已存在（创建时唯一性校验）。
     *
     * @param code    岗位编码
     * @param deleted 删除标记
     * @return 是否存在
     */
    boolean existsByCodeAndDeleted(String code, Integer deleted);
}
