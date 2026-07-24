package com.omni.scaffolding.modules.open.repository;

import com.omni.scaffolding.modules.open.entity.OpenApiEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 开放接口目录 JPA 仓库（主库写）。
 */
public interface OpenApiEndpointRepository extends JpaRepository<OpenApiEndpoint, Long> {

    /**
     * 按主键查未删除记录。
     */
    Optional<OpenApiEndpoint> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 编码是否已存在（未删除）。
     */
    boolean existsByCodeAndDeleted(String code, Integer deleted);

    /**
     * 编码是否被其他记录占用（更新时排除自身）。
     */
    boolean existsByCodeAndDeletedAndIdNot(String code, Integer deleted, Long id);
}
