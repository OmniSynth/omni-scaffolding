package com.omni.scaffolding.modules.open.entity;

import com.omni.scaffolding.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 开放 API 客户端实体（JPA 写模型）。
 *
 * <p>对应表 {@code open_api_client}。首期用 {@code api_key_hash} 做 {@code X-Api-Key} 校验；
 * {@code access_key}/{@code secret_hash} 预留给二期 HMAC 签名。
 * IP 白名单、接口绑定分别在 {@code open_api_client_ip}、{@code open_api_client_endpoint}。
 */
@Getter
@Setter
@Entity
@Table(name = "open_api_client")
public class OpenApiClient extends BaseAuditableEntity {

    /**
     * 主键。
     */
    @Id
    private Long id;

    /**
     * 客户端名称（第三方标识）。
     */
    @Column(nullable = false, length = 128)
    private String name;

    /**
     * API Key 的 SHA-256；明文仅创建/重置时返回一次。
     */
    @Column(name = "api_key_hash", nullable = false, length = 64)
    private String apiKeyHash;

    /**
     * 公开 AccessKey（可展示）；二期签名场景下作为 appId。
     */
    @Column(name = "access_key", nullable = false, length = 64)
    private String accessKey;

    /**
     * AccessSecret 的 SHA-256；二期签名用，首期仅落库。
     */
    @Column(name = "secret_hash", length = 64)
    private String secretHash;

    /**
     * 日调用上限；{@code null} 或 ≤0 表示不限制。
     */
    @Column(name = "daily_limit")
    private Integer dailyLimit;

    /**
     * 每秒请求上限（QPS）；{@code null} 或 ≤0 表示不限制。
     */
    @Column(name = "qps_limit")
    private Integer qpsLimit;

    /**
     * 过期时间；空表示不过期。
     */
    @Column(name = "expire_at")
    private Instant expireAt;

    /**
     * 备注说明。
     */
    @Column(length = 255)
    private String remark;

    /**
     * 是否启用。
     */
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * 逻辑删除：0 正常，1 已删除。
     */
    @Column(nullable = false)
    private Integer deleted = 0;
}
