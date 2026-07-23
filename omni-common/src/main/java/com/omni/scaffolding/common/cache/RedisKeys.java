package com.omni.scaffolding.common.cache;

/**
 * 业务侧直接操作 Redis 时的 Key 常量与组装方法。
 *
 * <p>禁止在业务代码中拼接魔法前缀；Spring Cache 名称见 {@link CacheNames}。
 */
public final class RedisKeys {

    /**
     * IP 白名单接口今日访问计数前缀：{@code omni:ipwl:{yyyyMMdd}:{ip}}。
     */
    public static final String IP_WHITELIST_VISIT_PREFIX = "omni:ipwl:";

    /**
     * 当日访问合计后缀（与 IP 并列）。
     */
    public static final String IP_WHITELIST_VISIT_TOTAL_SUFFIX = "__total__";

    /**
     * 在线会话 JSON 前缀：{@code omni:online:session:{jti}}。
     */
    public static final String ONLINE_SESSION_PREFIX = "omni:online:session:";

    /**
     * 用户会话 jti 集合前缀：{@code omni:online:user:{userId}}。
     */
    public static final String ONLINE_USER_PREFIX = "omni:online:user:";

    /**
     * 全部在线 jti 索引集合。
     */
    public static final String ONLINE_INDEX = "omni:online:index";

    /**
     * 踢下线 / 登出黑名单前缀：{@code omni:online:blacklist:{jti}}。
     */
    public static final String ONLINE_BLACKLIST_PREFIX = "omni:online:blacklist:";

    /**
     * 登录 nonce 防重放前缀：{@code login:nonce:{nonce}}。
     */
    public static final String LOGIN_NONCE_PREFIX = "login:nonce:";

    /**
     * 登录 IP 限流前缀：{@code login:ip:{ip}}。
     */
    public static final String LOGIN_IP_RATE_PREFIX = "login:ip:";

    /**
     * Demo 商品 SKU 分布式锁前缀：{@code lock:product:sku:{sku}}。
     */
    public static final String LOCK_PRODUCT_SKU_PREFIX = "lock:product:sku:";

    private RedisKeys() {
    }

    /**
     * 组装某日某 IP 的访问计数 Key。
     *
     * @param day 日期 {@code yyyyMMdd}
     * @param ip  客户端 IP
     * @return Redis Key
     */
    public static String ipWhitelistVisit(String day, String ip) {
        return IP_WHITELIST_VISIT_PREFIX + day + ":" + ip;
    }

    /**
     * 组装某日访问合计 Key。
     *
     * @param day 日期 {@code yyyyMMdd}
     * @return Redis Key
     */
    public static String ipWhitelistVisitTotal(String day) {
        return IP_WHITELIST_VISIT_PREFIX + day + ":" + IP_WHITELIST_VISIT_TOTAL_SUFFIX;
    }

    /**
     * 某日访问 Key 的公共前缀（含末尾冒号），用于截取 IP 后缀。
     *
     * @param day 日期 {@code yyyyMMdd}
     * @return 前缀，如 {@code omni:ipwl:20260722:}
     */
    public static String ipWhitelistVisitDayPrefix(String day) {
        return IP_WHITELIST_VISIT_PREFIX + day + ":";
    }

    /**
     * 某日访问 Key 的 SCAN 匹配模式。
     *
     * @param day 日期 {@code yyyyMMdd}
     * @return 模式，如 {@code omni:ipwl:20260722:*}
     */
    public static String ipWhitelistVisitDayPattern(String day) {
        return IP_WHITELIST_VISIT_PREFIX + day + ":*";
    }

    /**
     * 在线会话 Key。
     *
     * @param jti JWT ID
     * @return Redis Key
     */
    public static String onlineSession(String jti) {
        return ONLINE_SESSION_PREFIX + jti;
    }

    /**
     * 用户在线 jti 集合 Key。
     *
     * @param userId 用户主键
     * @return Redis Key
     */
    public static String onlineUser(Long userId) {
        return ONLINE_USER_PREFIX + userId;
    }

    /**
     * 会话黑名单 Key。
     *
     * @param jti JWT ID
     * @return Redis Key
     */
    public static String onlineBlacklist(String jti) {
        return ONLINE_BLACKLIST_PREFIX + jti;
    }

    /**
     * 登录 nonce Key。
     *
     * @param nonce 一次性随机串
     * @return Redis Key
     */
    public static String loginNonce(String nonce) {
        return LOGIN_NONCE_PREFIX + nonce;
    }

    /**
     * 登录 IP 限流 Key。
     *
     * @param ip 客户端 IP
     * @return Redis Key
     */
    public static String loginIpRate(String ip) {
        return LOGIN_IP_RATE_PREFIX + ip;
    }

    /**
     * Demo 商品 SKU 锁 Key。
     *
     * @param sku 商品 SKU
     * @return Redis Key
     */
    public static String lockProductSku(String sku) {
        return LOCK_PRODUCT_SKU_PREFIX + sku;
    }
}
