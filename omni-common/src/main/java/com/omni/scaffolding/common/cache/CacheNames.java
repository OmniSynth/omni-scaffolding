package com.omni.scaffolding.common.cache;

/**
 * Spring Cache 缓存名（落 Redis 时作为 key 前缀，形如 {@code cacheName::entryKey}）。
 *
 * <p>禁止在业务代码中书写魔法字符串，统一引用本类常量。
 */
public final class CacheNames {

    /** 系统参数按键取值缓存。 */
    public static final String SYS_CONFIG = "sysConfig";

    /** IP 白名单启用列表缓存。 */
    public static final String IP_WHITELIST = "ipWhitelist";

    /** 用户详情缓存。 */
    public static final String USERS = "users";

    /** 用户动态权限缓存。 */
    public static final String USER_PERMISSIONS = "userPermissions";

    /** 字典选项缓存。 */
    public static final String DICT_OPTIONS = "dictOptions";

    /** Demo 商品类目统计缓存。 */
    public static final String PRODUCT_STATS = "productStats";

    private CacheNames() {
    }
}
