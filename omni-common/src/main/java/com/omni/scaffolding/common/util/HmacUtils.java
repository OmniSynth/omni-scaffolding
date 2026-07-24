package com.omni.scaffolding.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

/**
 * HMAC 消息认证码工具。
 *
 * <p>登录等业务加签优先使用 {@link SignUtils#hmacSha256Hex(String, String)}；
 * 本类提供通用 HMAC-SHA256 / HMAC-SHA512，便于接口验签、Webhook 等场景复用。
 */
public final class HmacUtils {

    private static final HexFormat HEX = HexFormat.of();

    private HmacUtils() {
    }

    /**
     * HMAC-SHA256，返回小写十六进制。
     *
     * @param secret  密钥，不可为空
     * @param payload 原文，{@code null} 按空串
     * @return 64 位小写 hex
     */
    public static String sha256Hex(String secret, String payload) {
        return HEX.formatHex(sha256(bytes(secret, "secret"), bytesNullable(payload)));
    }

    /**
     * HMAC-SHA256。
     *
     * @param secret  密钥字节
     * @param payload 原文字节
     * @return 32 字节 MAC
     */
    public static byte[] sha256(byte[] secret, byte[] payload) {
        return hmac("HmacSHA256", secret, payload);
    }

    /**
     * HMAC-SHA512，返回小写十六进制。
     *
     * @param secret  密钥，不可为空
     * @param payload 原文，{@code null} 按空串
     * @return 128 位小写 hex
     */
    public static String sha512Hex(String secret, String payload) {
        return HEX.formatHex(sha512(bytes(secret, "secret"), bytesNullable(payload)));
    }

    /**
     * HMAC-SHA512。
     *
     * @param secret  密钥字节
     * @param payload 原文字节
     * @return 64 字节 MAC
     */
    public static byte[] sha512(byte[] secret, byte[] payload) {
        return hmac("HmacSHA512", secret, payload);
    }

    private static byte[] hmac(String algorithm, byte[] secret, byte[] payload) {
        if (secret == null || secret.length == 0) {
            throw new IllegalArgumentException("hmac secret must not be empty");
        }
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret, algorithm));
            return mac.doFinal(payload == null ? new byte[0] : payload);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(algorithm + " failed", ex);
        }
    }

    private static byte[] bytes(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] bytesNullable(String text) {
        return (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
    }
}
