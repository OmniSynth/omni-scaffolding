package com.omni.scaffolding.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 请求加签工具（HMAC-SHA256）。
 *
 * <p>用于登录等匿名接口防脚本爆破：客户端用共享密钥对时间戳/nonce/业务字段签名。
 */
public final class SignUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private SignUtils() {
    }

    /**
     * 计算 HMAC-SHA256，返回小写十六进制字符串。
     *
     * @param secret  共享密钥，不可为空
     * @param payload 待签名原文
     * @return 小写十六进制摘要
     */
    public static String hmacSha256Hex(String secret, String payload) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("sign secret must not be blank");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] raw = mac.doFinal((payload == null ? "" : payload).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception ex) {
            throw new IllegalStateException("hmac sha256 failed", ex);
        }
    }

    /**
     * 组装登录签名原文：{@code timestamp\\nnonce\\nusername\\npassword}。
     *
     * @param timestamp 毫秒时间戳字符串
     * @param nonce     一次性随机串
     * @param username  用户名
     * @param password  密码
     * @return 换行分隔的签名原文
     */
    public static String loginPayload(String timestamp, String nonce, String username, String password) {
        return (timestamp == null ? "" : timestamp)
                + "\n"
                + (nonce == null ? "" : nonce)
                + "\n"
                + (username == null ? "" : username)
                + "\n"
                + (password == null ? "" : password);
    }

    /**
     * 恒定时间比较，降低时序攻击面。
     *
     * @param expected 期望的十六进制摘要
     * @param actual   客户端提交的摘要
     * @return {@code true} 完全一致
     */
    public static boolean equalsHex(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = actual.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
