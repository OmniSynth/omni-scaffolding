package com.omni.scaffolding.common.cache;

/**
 * Spring Cache 条目键常量（配合 {@link CacheNames} 使用）。
 */
public final class CacheKeys {

    /**
     * {@link CacheNames#IP_WHITELIST} 下：全部启用 IP 列表。
     */
    public static final String IP_WHITELIST_ENABLED = "enabled";

    private CacheKeys() {
    }
}
