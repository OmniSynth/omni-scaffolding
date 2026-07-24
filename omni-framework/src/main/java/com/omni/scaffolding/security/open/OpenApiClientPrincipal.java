package com.omni.scaffolding.security.open;

import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * 开放 API 客户端认证主体。
 *
 * <p>由 {@link OpenApiAuthFilter} 在校验通过后写入 {@code SecurityContext}，
 * 业务 Controller 可据此识别第三方调用方。
 */
@Getter
public class OpenApiClientPrincipal implements AuthenticatedPrincipal, Serializable {

    /**
     * 客户端主键。
     */
    private final Long clientId;

    /**
     * 客户端名称。
     */
    private final String name;

    /**
     * 公开 AccessKey。
     */
    private final String accessKey;

    /**
     * 预留权限集合（首期可为空，过滤器会补默认 {@code OPEN_API}）。
     */
    private final Set<String> authorities;

    /**
     * @param clientId    客户端主键
     * @param name        客户端名称
     * @param accessKey   公开 AccessKey
     * @param authorities 预留权限集合
     */
    public OpenApiClientPrincipal(Long clientId, String name, String accessKey, Set<String> authorities) {
        this.clientId = clientId;
        this.name = name;
        this.accessKey = accessKey;
        this.authorities = authorities == null ? Set.of() : Collections.unmodifiableSet(authorities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }
}
