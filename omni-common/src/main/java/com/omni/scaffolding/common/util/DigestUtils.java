package com.omni.scaffolding.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 消息摘要工具（MD5 / SHA 系列）。
 *
 * <p>MD5 / SHA-1 仅适合校验、指纹等非安全场景；密码存储请用 BCrypt，完整性校验优先 SHA-256。
 * 本类方法为单向摘要，不可逆「解密」。
 */
public final class DigestUtils {

    private static final HexFormat HEX = HexFormat.of();

    private DigestUtils() {
    }

    /**
     * MD5 十六进制摘要（小写）。
     *
     * @param text 原文，{@code null} 按空串
     * @return 32 位小写 hex
     */
    public static String md5Hex(String text) {
        return HEX.formatHex(digest("MD5", bytes(text)));
    }

    /**
     * MD5 摘要。
     *
     * @param data 原始字节
     * @return 16 字节摘要
     */
    public static byte[] md5(byte[] data) {
        return digest("MD5", data);
    }

    /**
     * SHA-1 十六进制摘要（小写）。
     *
     * @param text 原文，{@code null} 按空串
     * @return 40 位小写 hex
     */
    public static String sha1Hex(String text) {
        return HEX.formatHex(digest("SHA-1", bytes(text)));
    }

    /**
     * SHA-256 十六进制摘要（小写）。
     *
     * @param text 原文，{@code null} 按空串
     * @return 64 位小写 hex
     */
    public static String sha256Hex(String text) {
        return HEX.formatHex(digest("SHA-256", bytes(text)));
    }

    /**
     * SHA-256 摘要。
     *
     * @param data 原始字节
     * @return 32 字节摘要
     */
    public static byte[] sha256(byte[] data) {
        return digest("SHA-256", data);
    }

    /**
     * SHA-512 十六进制摘要（小写）。
     *
     * @param text 原文，{@code null} 按空串
     * @return 128 位小写 hex
     */
    public static String sha512Hex(String text) {
        return HEX.formatHex(digest("SHA-512", bytes(text)));
    }

    /**
     * 指定算法的十六进制摘要（小写）。
     *
     * @param algorithm 如 {@code MD5}、{@code SHA-256}
     * @param data      原始字节，不可为 null
     * @return 小写 hex
     */
    public static String hex(String algorithm, byte[] data) {
        return HEX.formatHex(digest(algorithm, data));
    }

    private static byte[] digest(String algorithm, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("digest data must not be null");
        }
        try {
            return MessageDigest.getInstance(algorithm).digest(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("digest algorithm unavailable: " + algorithm, ex);
        }
    }

    private static byte[] bytes(String text) {
        return (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
    }
}
