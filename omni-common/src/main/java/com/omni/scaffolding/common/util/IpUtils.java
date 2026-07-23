package com.omni.scaffolding.common.util;

/**
 * 客户端 IP 解析工具。
 *
 * <p>优先读取代理头，兼容 Nginx / 网关转发场景。
 */
public final class IpUtils {

    private static final int MAX_LEN = 64;

    private IpUtils() {
    }

    /**
     * 从常见请求头解析客户端 IP。
     *
     * @param xForwardedFor {@code X-Forwarded-For}，可为空
     * @param xRealIp       {@code X-Real-IP}，可为空
     * @param remoteAddr    {@code HttpServletRequest#getRemoteAddr()}
     * @return 解析后的客户端 IP，最长 64 字符
     */
    public static String resolveClientIp(String xForwardedFor, String xRealIp, String remoteAddr) {
        String ip = firstForwarded(xForwardedFor);
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = xRealIp;
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = remoteAddr;
        }
        if (ip == null) {
            return "";
        }
        ip = ip.trim();
        return ip.length() > MAX_LEN ? ip.substring(0, MAX_LEN) : ip;
    }

    /**
     * 从 {@code X-Forwarded-For} 取第一个客户端 IP（逗号分隔时取首段）。
     *
     * @param xForwardedFor 代理转发头
     * @return 首个 IP，空白时为 null
     */
    private static String firstForwarded(String xForwardedFor) {
        if (xForwardedFor == null || xForwardedFor.isBlank()) {
            return null;
        }
        int comma = xForwardedFor.indexOf(',');
        return comma >= 0 ? xForwardedFor.substring(0, comma).trim() : xForwardedFor.trim();
    }
}
