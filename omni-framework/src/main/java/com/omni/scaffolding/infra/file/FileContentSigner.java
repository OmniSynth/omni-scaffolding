package com.omni.scaffolding.infra.file;

import com.omni.scaffolding.common.util.SignUtils;
import com.omni.scaffolding.config.OmniFileProperties;
import com.omni.scaffolding.config.OmniSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * 文件内容短时签名（HMAC-SHA256）。
 *
 * <p>用于 {@code <img>} 等无法携带 Bearer 的场景：签发
 * {@code /api/system/files/{id}/content?expire=&sign=}，由 {@link com.omni.scaffolding.modules.system.service.FileService}
 * 校验签名或 JWT。
 *
 * <p>签名原文：{@code fileId + "\n" + expireEpochSeconds}。
 */
@Component
@RequiredArgsConstructor
public class FileContentSigner {

    private final OmniFileProperties fileProperties;
    private final OmniSecurityProperties securityProperties;

    /**
     * 默认过期时间（当前秒 + TTL，TTL 至少 30 秒）。
     *
     * @return Unix 纪元秒
     */
    public long defaultExpireEpoch() {
        return Instant.now().getEpochSecond() + Math.max(30L, fileProperties.getSignTtlSeconds());
    }

    /**
     * 计算签名（小写十六进制）。
     *
     * @param fileId             文件主键
     * @param expireEpochSeconds 过期时间（Unix 秒）
     * @return HMAC-SHA256 hex
     */
    public String sign(long fileId, long expireEpochSeconds) {
        return SignUtils.hmacSha256Hex(secret(), payload(fileId, expireEpochSeconds));
    }

    /**
     * 校验签名是否有效且未过期。
     *
     * @param fileId             文件主键
     * @param expireEpochSeconds 过期时间；{@code null} 视为无效
     * @param sign               客户端提交的签名
     * @return {@code true} 表示可通过签名访问
     */
    public boolean verify(long fileId, Long expireEpochSeconds, String sign) {
        if (expireEpochSeconds == null || !StringUtils.hasText(sign)) {
            return false;
        }
        if (expireEpochSeconds < Instant.now().getEpochSecond()) {
            return false;
        }
        String expected = sign(fileId, expireEpochSeconds);
        return SignUtils.equalsHex(expected, sign.trim().toLowerCase());
    }

    /**
     * 组装可直接用于 {@code img.src} 的相对路径（含 query）。
     *
     * @param fileId             文件主键
     * @param expireEpochSeconds 过期时间
     * @param sign               签名
     * @return 如 {@code /api/system/files/1/content?expire=...&sign=...}
     */
    public String buildContentPath(long fileId, long expireEpochSeconds, String sign) {
        return "/api/system/files/" + fileId + "/content?expire=" + expireEpochSeconds + "&sign=" + sign;
    }

    private String secret() {
        return fileProperties.resolveSignSecret(securityProperties.getJwt().getSecret());
    }

    private static String payload(long fileId, long expireEpochSeconds) {
        return fileId + "\n" + expireEpochSeconds;
    }
}
