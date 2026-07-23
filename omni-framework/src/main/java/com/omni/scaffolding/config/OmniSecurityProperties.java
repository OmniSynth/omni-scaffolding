package com.omni.scaffolding.config;

import com.omni.scaffolding.security.xss.XssMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全相关外部化配置，前缀 {@code omni.security}。
 *
 * <p>生产环境务必通过环境变量覆盖 {@code jwt.secret}（至少 32 字节），勿使用仓库中的开发默认值。
 */
@Data
@Component
@ConfigurationProperties(prefix = "omni.security")
public class OmniSecurityProperties {

    private Jwt jwt = new Jwt();

    /**
     * 匿名可访问路径（登录、健康检查、Swagger 等）。
     */
    private List<String> permitAll = new ArrayList<>();

    /**
     * XSS 输入防护配置。
     */
    private Xss xss = new Xss();

    /**
     * 动态权限：开启后每次请求从库/缓存重载权限，角色变更无需重新登录；关闭则仅信任 JWT（性能更好）。
     */
    private DynamicPermission dynamicPermission = new DynamicPermission();

    /**
     * 登录接口 HMAC 加签与防爆破（时间窗 + nonce + IP 限流）。
     */
    private Sign sign = new Sign();

    /**
     * 全局 IP 白名单兜底（逗号分隔），供 {@code @IpWhitelist} 使用；
     * 优先读表 {@code sys_ip_whitelist}，表无启用记录时才回退到本配置。
     */
    private String ipWhitelist = "127.0.0.1,0:0:0:0:0:0:0:1";

    @Data
    public static class Jwt {
        /**
         * HMAC 签名密钥。
         */
        private String secret;
        /**
         * Token 过期毫秒数，默认 24h。
         */
        private long expirationMs = 86_400_000L;
    }

    /**
     * XSS 过滤器与清洗策略。
     */
    @Data
    public static class Xss {

        /**
         * 是否启用 XSS 请求清洗过滤器，默认开启。
         */
        private boolean enabled = true;

        /**
         * 清洗模式：{@link XssMode#STRIP}（默认）或 {@link XssMode#ESCAPE}。
         */
        private XssMode mode = XssMode.STRIP;

        /**
         * 不经过 XSS 清洗的路径 Ant 模式（如文档、健康检查、大文件上传入口）。
         */
        private List<String> excludePathPatterns = new ArrayList<>(List.of(
                "/actuator/**",
                "/druid/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        ));
    }

    /**
     * 动态权限开关。
     */
    @Data
    public static class DynamicPermission {

        /**
         * 是否启用动态权限。默认关闭，避免每请求查权限带来的开销。
         */
        private boolean enabled = false;
    }

    /**
     * 登录加签配置。
     *
     * <p>请求头：{@code X-Omni-Timestamp} / {@code X-Omni-Nonce} / {@code X-Omni-Sign}；
     * 签名原文：{@code timestamp\\nnonce\\nusername\\npassword}，算法 HMAC-SHA256（小写 hex）。
     */
    @Data
    public static class Sign {

        /**
         * 是否启用登录加签，默认开启。
         */
        private boolean enabled = true;

        /**
         * 前后端共享 HMAC 密钥；生产务必用环境变量覆盖。
         */
        private String secret;

        /**
         * 允许的时间偏差（秒），默认 5 分钟。
         */
        private long skewSeconds = 300L;

        /**
         * 同一 IP 在窗口内允许的登录尝试次数，默认 20。
         */
        private int ipLimit = 20;

        /**
         * IP 限流窗口秒数，默认 60。
         */
        private int ipWindowSeconds = 60;
    }
}
