package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 系统参数 JPA 写仓储。
 */
public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {

    /**
     * 按主键查询未删除记录。
     *
     * @param id      主键
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 实体，不存在则为空
     */
    Optional<SysConfig> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 按参数键查询未删除记录。
     *
     * @param configKey 参数键
     * @param deleted   删除标记，通常传 {@code 0}
     * @return 实体，不存在则为空
     */
    Optional<SysConfig> findByConfigKeyAndDeleted(String configKey, Integer deleted);

    /**
     * 判断未删除范围内是否已存在该参数键。
     *
     * @param configKey 参数键
     * @param deleted   删除标记，通常传 {@code 0}
     * @return 存在则为 {@code true}
     */
    boolean existsByConfigKeyAndDeleted(String configKey, Integer deleted);
}
