package com.omni.scaffolding.common.security;

/**
 * 开放 API 请求头与路径常量。
 */
public final class OpenApiHeaders {

    /**
     * 第三方携带的 API Key 请求头。
     */
    public static final String API_KEY = "X-Api-Key";

    /**
     * 开放业务接口前缀（不含管理端）。
     */
    public static final String OPEN_PATH_PREFIX = "/api/open/";

    /**
     * 管理端前缀（走 JWT，不走 API Key）。
     */
    public static final String ADMIN_PATH_PREFIX = "/api/open/admin/";

    private OpenApiHeaders() {
    }
}
