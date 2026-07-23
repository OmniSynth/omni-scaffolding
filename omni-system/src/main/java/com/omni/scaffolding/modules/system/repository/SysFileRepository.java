package com.omni.scaffolding.modules.system.repository;

import com.omni.scaffolding.modules.system.entity.SysFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 文件元数据仓储（JPA 写 / 按主键读）。
 */
public interface SysFileRepository extends JpaRepository<SysFile, Long> {

    /**
     * 按主键与逻辑删除标记查询。
     *
     * @param id      文件 ID
     * @param deleted 删除标记，通常传 {@code 0}
     * @return 文件实体
     */
    Optional<SysFile> findByIdAndDeleted(Long id, Integer deleted);
}
