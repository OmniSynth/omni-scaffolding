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
     * 登录图形验证码。
     */
    private Captcha captcha = new Captcha();

    /**
     * 登录失败锁定（按用户名）。
     */
    private LoginLock loginLock = new LoginLock();

    /**
     * 密码复杂度与强制改密策略。
     */
    private PasswordPolicy passwordPolicy = new PasswordPolicy();

    /**
     * 全局 IP 白名单兜底（逗号分隔），供 {@code @IpWhitelist} 使用；
     * 优先读表 {@code sys_ip_whitelist}，表无启用记录时才回退到本配置。
     */
    private String ipWhitelist = "127.0.0.1,0:0:0:0:0:0:0:1";

    /**
     * 浏览器 CORS；生产需把站点 Origin（含协议）加入白名单，否则登录等带 Origin 的请求会被拒。
     */
    private Cors cors = new Cors();

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

    /**
     * 登录图形验证码。
     */
    @Data
    public static class Captcha {

        /**
         * 是否启用；关闭时登录可不传 captcha。
         */
        private boolean enabled = true;

        /**
         * 验证码 Redis TTL（秒）。
         */
        private int ttlSeconds = 120;

        /**
         * 验证码字符数。
         */
        private int length = 4;
    }

    /**
     * 连续登录失败后锁定账号（Redis）。
     */
    @Data
    public static class LoginLock {

        /**
         * 是否启用。
         */
        private boolean enabled = true;

        /**
         * 窗口内最大失败次数。
         */
        private int maxFailures = 5;

        /**
         * 锁定时长（秒），同时作为失败计数窗口。
         */
        private int lockSeconds = 900;
    }

    /**
     * 密码策略：创建/重置/本人修改时校验。
     */
    @Data
    public static class PasswordPolicy {

        private int minLength = 6;

        private int maxLength = 64;

        private boolean requireUppercase = false;

        private boolean requireLowercase = false;

        private boolean requireDigit = false;

        private boolean requireSpecial = false;

        /**
         * 新建用户后要求首次登录强制改密。
         */
        private boolean forceChangeOnCreate = true;

        /**
         * 管理员重置密码后要求强制改密。
         */
        private boolean forceChangeOnReset = true;
    }

    /**
     * CORS 白名单。
     *
     * <p>支持 {@link CorsConfiguration#setAllowedOriginPatterns} 通配（如 {@code http://localhost:*}）。
     * nginx 同源反代时浏览器仍会带 Origin，未命中白名单会返回 {@code Invalid CORS request}。
     */
    @Data
    public static class Cors {

        /**
         * 允许的 Origin 模式列表；默认仅本机 / 局域网 Vite 开发端口。
         */
        private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*.*:*",
                "http://10.*.*.*:*",
                "http://172.*.*.*:*"
        ));
    }
}
