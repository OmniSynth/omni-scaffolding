package com.omni.scaffolding.security.sign;

/**
 * 登录加签请求头常量。
 */
public final class LoginSignHeaders {

    /** 客户端毫秒时间戳请求头。 */
    public static final String TIMESTAMP = "X-Omni-Timestamp";

    /** 一次性随机串，防重放。 */
    public static final String NONCE = "X-Omni-Nonce";

    /** HMAC-SHA256 签名（小写十六进制）。 */
    public static final String SIGN = "X-Omni-Sign";

    private LoginSignHeaders() {
    }
}
