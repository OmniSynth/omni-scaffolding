package com.omni.scaffolding.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 生产启动配置硬校验：不合规直接失败并打出运维可读中文原因。
 *
 * <p>在 admin 密码 bootstrap 之前执行，避免「服务起来了但登录必挂」。
 */
@Slf4j
@Component
@Profile("prod")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ProdSecurityConfigValidator implements ApplicationRunner {

    private static final List<String> FORBIDDEN_SECRETS = List.of("admin123", "123456");

    private final OmniSecurityProperties securityProperties;

    @Override
    public void run(ApplicationArguments args) {
        List<String> errors = new ArrayList<>();

        String jwt = securityProperties.getJwt() == null ? null : securityProperties.getJwt().getSecret();
        if (!StringUtils.hasText(jwt)) {
            errors.add("缺少 OMNI_JWT_SECRET（omni.security.jwt.secret）");
        } else {
            int jwtBytes = jwt.getBytes(StandardCharsets.UTF_8).length;
            if (jwtBytes < 32) {
                errors.add("OMNI_JWT_SECRET 至少 32 字节，当前=" + jwtBytes
                        + "。示例：openssl rand -base64 32");
            }
        }

        OmniSecurityProperties.Sign sign = securityProperties.getSign();
        if (sign != null && sign.isEnabled()) {
            String signSecret = sign.getSecret();
            if (!StringUtils.hasText(signSecret)) {
                errors.add("缺少 OMNI_SIGN_SECRET（omni.security.sign.secret）；前端 VITE_OMNI_SIGN_SECRET 必须与之一致");
            } else {
                String trimmed = signSecret.trim();
                if (FORBIDDEN_SECRETS.contains(trimmed)) {
                    errors.add("OMNI_SIGN_SECRET 禁止使用 admin123 / 123456（与历史演示登录密码相同，运维极易当成登录密码）。"
                            + "请换成独立随机串，并同步前端 VITE_OMNI_SIGN_SECRET 后重新 npm run build");
                }
                if (trimmed.length() < 8) {
                    errors.add("OMNI_SIGN_SECRET 至少 8 位，当前长度=" + trimmed.length());
                }
            }
        }

        String adminPwd = securityProperties.getBootstrapAdminPassword();
        if (!StringUtils.hasText(adminPwd)) {
            errors.add("缺少 OMNI_ADMIN_INITIAL_PASSWORD（首次启动用于替换演示 admin 密码；之后登录也用这个值，不是 admin123）");
        } else {
            String trimmed = adminPwd.trim();
            if (trimmed.length() < 12) {
                errors.add("OMNI_ADMIN_INITIAL_PASSWORD 至少 12 位，当前长度=" + trimmed.length());
            }
            if (FORBIDDEN_SECRETS.contains(trimmed)) {
                errors.add("OMNI_ADMIN_INITIAL_PASSWORD 不能使用演示密码 admin123 / 123456");
            }
            if (sign != null && StringUtils.hasText(sign.getSecret())
                    && trimmed.equals(sign.getSecret().trim())) {
                errors.add("OMNI_ADMIN_INITIAL_PASSWORD 不能与 OMNI_SIGN_SECRET 相同（一个是登录密码，一个是加签密钥，混用必乱）");
            }
        }

        List<String> cors = securityProperties.getCors() == null
                ? List.of()
                : securityProperties.getCors().getAllowedOriginPatterns();
        if (CollectionUtils.isEmpty(cors)
                || cors.stream().noneMatch(StringUtils::hasText)) {
            errors.add("缺少 OMNI_CORS_ORIGINS（浏览器 Origin，例如 https://your.domain）；否则登录会 403 Invalid CORS request");
        }

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append('\n')
                    .append("========== [prod] 部署配置不合规，已拒绝启动 ==========\n");
            for (int i = 0; i < errors.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(errors.get(i)).append('\n');
            }
            sb.append("请修正 Jenkins/启动脚本中的环境变量后重新发布。\n")
                    .append("======================================================");
            log.error(sb.toString());
            throw new IllegalStateException("prod 安全配置校验失败，共 " + errors.size()
                    + " 项，详见上方日志");
        }

        log.warn("""

                ========== [prod] 安全配置校验通过 ==========
                登录账号     : admin
                登录密码     : 环境变量 OMNI_ADMIN_INITIAL_PASSWORD（不是 admin123，也不是 OMNI_SIGN_SECRET）
                加签密钥     : OMNI_SIGN_SECRET（仅给前后端 HMAC，长度={}；须 = 前端 VITE_OMNI_SIGN_SECRET）
                JWT 密钥     : OMNI_JWT_SECRET（长度={} 字节）
                CORS         : {}
                ============================================
                """,
                securityProperties.getSign().getSecret().trim().length(),
                securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8).length,
                cors);
    }
}
