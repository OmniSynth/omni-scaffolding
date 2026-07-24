package com.omni.scaffolding.modules.open.repository;

import com.omni.scaffolding.modules.open.entity.OpenApiClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 开放 API 客户端 JPA 仓库（主库写）。
 */
public interface OpenApiClientRepository extends JpaRepository<OpenApiClient, Long> {

    /**
     * 按主键查未删除记录。
     */
    Optional<OpenApiClient> findByIdAndDeleted(Long id, Integer deleted);

    /**
     * 按 API Key 哈希查未删除客户端（鉴权用）。
     */
    Optional<OpenApiClient> findByApiKeyHashAndDeleted(String apiKeyHash, Integer deleted);
}
