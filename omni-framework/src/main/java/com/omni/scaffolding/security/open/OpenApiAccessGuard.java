package com.omni.scaffolding.security.open;

/**
 * 开放 API 访问守卫：校验 API Key、IP、接口授权与配额。
 *
 * <p>接口定义在 framework，实现位于 {@code modules.open}，避免反向依赖。
 */
public interface OpenApiAccessGuard {

    /**
     * 鉴权并授权；失败时抛出 {@link com.omni.scaffolding.common.exception.BusinessException}。
     *
     * @param apiKey   请求头中的明文 API Key
     * @param clientIp 客户端 IP
     * @param method   HTTP 方法
     * @param path     请求路径（含 context path 已剥离）
     * @return 认证主体
     */
    OpenApiClientPrincipal authenticate(String apiKey, String clientIp, String method, String path);
}
