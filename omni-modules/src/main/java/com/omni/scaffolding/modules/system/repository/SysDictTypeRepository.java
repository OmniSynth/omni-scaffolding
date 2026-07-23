package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysDictType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 字典类型 JPA 写仓储。
 */
public interface SysDictTypeRepository extends JpaRepository<SysDictType, Long> {

    /**
     * 按主键查询未删除记录。
     *
     * @param id      主键
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 实体，不存在则为空
     */
    Optional<SysDictType> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 按类型编码查询未删除记录。
     *
     * @param code    类型编码
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 实体，不存在则为空
     */
    Optional<SysDictType> findByCodeAndDeleted(String code, Integer deleted);

    /**
     * 判断未删除范围内是否已存在该类型编码。
     *
     * @param code    类型编码
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 存在则为 {@code true}
     */
    boolean existsByCodeAndDeleted(String code, Integer deleted);
}
