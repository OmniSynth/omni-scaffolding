package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysDictData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 字典数据 JPA 写仓储。
 */
public interface SysDictDataRepository extends JpaRepository<SysDictData, Long> {

    /**
     * 按主键查询未删除记录。
     *
     * @param id      主键
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 实体，不存在则为空
     */
    Optional<SysDictData> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 按类型编码与存储值查询未删除记录。
     *
     * @param typeCode 类型编码
     * @param value    存储值
     * @param deleted  删除标记，通常传 {@code 0}
     * @return 实体，不存在则为空
     */
    Optional<SysDictData> findByTypeCodeAndValueAndDeleted(String typeCode, String value, Integer deleted);

    /**
     * 判断未删除范围内是否已存在该类型下的存储值。
     *
     * @param typeCode 类型编码
     * @param value    存储值
     * @param deleted  删除标记，通常传 {@code 0}
     * @return 存在则为 {@code true}
     */
    boolean existsByTypeCodeAndValueAndDeleted(String typeCode, String value, Integer deleted);
}
