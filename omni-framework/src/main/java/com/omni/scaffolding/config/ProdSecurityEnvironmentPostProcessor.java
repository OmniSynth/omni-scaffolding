package com.omni.scaffolding.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 生产环境变量硬校验：在容器 / Flyway / Quartz 启动前失败，并把原因打到控制台。
 *
 * <p>比 {@code ApplicationRunner} 更早：避免已打印 {@code Started} 再因配置退出。
 */
@Order(ConfigDataEnvironmentPostProcessor.ORDER + 30)
public class ProdSecurityEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final List<String> FORBIDDEN = List.of("admin123", "123456");

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 30;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isProd(environment)) {
            return;
        }

        List<String> errors = new ArrayList<>();

        String jwt = first(environment, "OMNI_JWT_SECRET", "omni.security.jwt.secret");
        if (!StringUtils.hasText(jwt)) {
            errors.add("缺少 OMNI_JWT_SECRET");
        } else {
            int bytes = jwt.getBytes(StandardCharsets.UTF_8).length;
            if (bytes < 32) {
                errors.add("OMNI_JWT_SECRET 至少 32 字节，当前=" + bytes + "。生成：openssl rand -base64 32");
            }
        }

        String signEnabled = environment.getProperty("omni.security.sign.enabled", "true");
        String sign = first(environment, "OMNI_SIGN_SECRET", "omni.security.sign.secret");
        if (!"false".equalsIgnoreCase(signEnabled)) {
            if (!StringUtils.hasText(sign)) {
                errors.add("缺少 OMNI_SIGN_SECRET（须与前端 VITE_OMNI_SIGN_SECRET 一致）");
            } else {
                String s = sign.trim();
                if (FORBIDDEN.contains(s)) {
                    errors.add("OMNI_SIGN_SECRET 禁止 admin123/123456（易与登录密码混淆）；请换独立随机串并重建前端");
                }
                if (s.length() < 8) {
                    errors.add("OMNI_SIGN_SECRET 至少 8 位，当前长度=" + s.length());
                }
            }
        }

        String admin = first(environment, "OMNI_ADMIN_INITIAL_PASSWORD", "omni.security.bootstrap-admin-password");
        if (!StringUtils.hasText(admin)) {
            errors.add("缺少 OMNI_ADMIN_INITIAL_PASSWORD（admin 登录密码；不是 admin123，也不是 OMNI_SIGN_SECRET）");
        } else {
            String a = admin.trim();
            if (a.length() < 12) {
                errors.add("OMNI_ADMIN_INITIAL_PASSWORD 至少 12 位，当前长度=" + a.length());
            }
            if (FORBIDDEN.contains(a)) {
                errors.add("OMNI_ADMIN_INITIAL_PASSWORD 不能使用 admin123/123456");
            }
            if (StringUtils.hasText(sign) && a.equals(sign.trim())) {
                errors.add("OMNI_ADMIN_INITIAL_PASSWORD 不能与 OMNI_SIGN_SECRET 相同");
            }
        }

        String cors = first(environment, "OMNI_CORS_ORIGINS", "omni.security.cors.allowed-origin-patterns[0]");
        if (!StringUtils.hasText(cors)) {
            // 列表属性在 env 阶段可能读不到下标，再扫一遍常见绑定
            cors = environment.getProperty("omni.security.cors.allowed-origin-patterns");
        }
        if (!StringUtils.hasText(cors)) {
            errors.add("缺少 OMNI_CORS_ORIGINS（例：https://your.domain），否则登录 403 Invalid CORS request");
        }

        if (errors.isEmpty()) {
            String ok = """

                    ========== [prod] 安全配置预检通过 ==========
                    登录：admin + OMNI_ADMIN_INITIAL_PASSWORD
                    禁止把 admin123 / OMNI_SIGN_SECRET 当登录密码
                    ============================================
                    """;
            System.out.println(ok);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append('\n')
                .append("========== [prod] 部署配置不合规，已拒绝启动 ==========\n");
        for (int i = 0; i < errors.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(errors.get(i)).append('\n');
        }
        sb.append("请修正 Jenkins/启动脚本环境变量后重新发布。\n")
                .append("======================================================\n");
        // 日志系统尚未就绪时，仍保证 Jenkins 控制台可见
        System.err.println(sb);
        throw new IllegalStateException(sb.toString());
    }

    private static boolean isProd(ConfigurableEnvironment environment) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            return true;
        }
        String active = environment.getProperty("spring.profiles.active", "");
        for (String profile : active.split(",")) {
            if ("prod".equals(profile.trim())) {
                return true;
            }
        }
        return false;
    }

    private static String first(ConfigurableEnvironment environment, String... keys) {
        for (String key : keys) {
            String v = environment.getProperty(key);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }
}
