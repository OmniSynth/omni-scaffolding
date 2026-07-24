package com.omni.scaffolding.common.util;

/**
 * 客户端 IP 解析工具。
 *
 * <p>当直连对端为本机回环或内网地址（Vite / Nginx 反代）时，优先读取代理头；
 * 公网直连则使用 {@code remoteAddr}，降低伪造 {@code X-Forwarded-For} 的影响。
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
        String peer = normalize(remoteAddr);
        String ip = null;
        // Vite→127.0.0.1、本机/内网 Nginx 反代时信任转发头
        if (isTrustedProxyPeer(peer)) {
            ip = firstForwarded(xForwardedFor);
            if (isBlankOrUnknown(ip)) {
                ip = xRealIp;
            }
        }
        if (isBlankOrUnknown(ip)) {
            ip = peer;
        }
        if (ip == null) {
            return "";
        }
        ip = normalize(ip);
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

    private static boolean isBlankOrUnknown(String ip) {
        return ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip.trim());
    }

    /**
     * 规范化 IP：去空白、剥离 IPv6 映射前缀；回环统一为 {@code 127.0.0.1}。
     */
    static String normalize(String ip) {
        if (ip == null) {
            return null;
        }
        String value = ip.trim();
        if (value.isEmpty()) {
            return value;
        }
        if (value.regionMatches(true, 0, "::ffff:", 0, 7)) {
            value = value.substring(7);
        }
        if ("::1".equals(value) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(value)) {
            return "127.0.0.1";
        }
        return value;
    }

    /**
     * 是否视为可信反代对端：回环或 RFC1918 私网。
     */
    static boolean isTrustedProxyPeer(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String value = normalize(ip);
        if (value.startsWith("127.")) {
            return true;
        }
        if (value.startsWith("10.")) {
            return true;
        }
        if (value.startsWith("192.168.")) {
            return true;
        }
        if (value.startsWith("172.")) {
            int secondDot = value.indexOf('.', 4);
            if (secondDot > 4) {
                try {
                    int second = Integer.parseInt(value.substring(4, secondDot));
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }
}
