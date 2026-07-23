package com.omni.scaffolding.config;

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 生产部署配置清单：一次性给出全部检查项的通过/失败，避免运维改一项再启动撞下一项。
 */
public final class ProdDeployConfigChecker {

    private static final List<String> FORBIDDEN = List.of("admin123", "123456");
    private static final AtomicBoolean ALREADY_RAN = new AtomicBoolean(false);

    private ProdDeployConfigChecker() {
    }

    /**
     * 是否按 prod 规则校验（多来源探测，避免 EnvironmentPostProcessor 阶段 activeProfiles 仍为空）。
     */
    public static boolean isProd(Environment environment, String... args) {
        if (environment != null) {
            for (String p : environment.getActiveProfiles()) {
                if ("prod".equalsIgnoreCase(p)) {
                    return true;
                }
            }
            if (containsProd(environment.getProperty("spring.profiles.active"))) {
                return true;
            }
            if (containsProd(environment.getProperty("SPRING_PROFILES_ACTIVE"))) {
                return true;
            }
        }
        if (containsProd(System.getenv("SPRING_PROFILES_ACTIVE"))) {
            return true;
        }
        if (containsProd(System.getProperty("spring.profiles.active"))) {
            return true;
        }
        if (args != null) {
            for (String arg : args) {
                if (arg == null) {
                    continue;
                }
                if (arg.startsWith("--spring.profiles.active=")) {
                    if (containsProd(arg.substring("--spring.profiles.active=".length()))) {
                        return true;
                    }
                }
                if (arg.startsWith("--spring.profiles.active:")) {
                    if (containsProd(arg.substring("--spring.profiles.active:".length()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 执行全部检查；有失败则打印完整清单并抛错。同一 JVM 只跑一次。
     */
    public static void requireValidOrThrow(Environment environment) {
        if (!ALREADY_RAN.compareAndSet(false, true)) {
            return;
        }
        List<CheckItem> items = evaluate(environment);
        String report = formatReport(items);
        boolean failed = items.stream().anyMatch(i -> !i.ok());
        if (failed) {
            System.err.println(report);
            throw new IllegalStateException(report);
        }
        System.out.println(report);
    }

    /**
     * 评估全部检查项（不抛错），供测试或二次展示。
     */
    public static List<CheckItem> evaluate(Environment environment) {
        List<CheckItem> items = new ArrayList<>();

        String jwt = first(environment, "OMNI_JWT_SECRET", "omni.security.jwt.secret");
        if (!StringUtils.hasText(jwt)) {
            items.add(fail("OMNI_JWT_SECRET", "未设置"));
        } else {
            int bytes = jwt.getBytes(StandardCharsets.UTF_8).length;
            if (bytes < 32) {
                items.add(fail("OMNI_JWT_SECRET", "至少 32 字节，当前=" + bytes + "；生成：openssl rand -base64 32"));
            } else {
                items.add(ok("OMNI_JWT_SECRET", "长度=" + bytes + " 字节"));
            }
        }

        String signEnabled = environment.getProperty("omni.security.sign.enabled", "true");
        String sign = first(environment, "OMNI_SIGN_SECRET", "omni.security.sign.secret");
        if ("false".equalsIgnoreCase(signEnabled)) {
            items.add(ok("OMNI_SIGN_SECRET", "加签已关闭，跳过"));
        } else if (!StringUtils.hasText(sign)) {
            items.add(fail("OMNI_SIGN_SECRET", "未设置；须与前端 VITE_OMNI_SIGN_SECRET 一致"));
        } else {
            String s = sign.trim();
            List<String> signProblems = new ArrayList<>();
            if (FORBIDDEN.contains(s)) {
                signProblems.add("禁止 admin123/123456（易与登录密码混淆）");
            }
            if (s.length() < 8) {
                signProblems.add("至少 8 位，当前=" + s.length());
            }
            if (signProblems.isEmpty()) {
                items.add(ok("OMNI_SIGN_SECRET", "长度=" + s.length() + "；须 = VITE_OMNI_SIGN_SECRET"));
            } else {
                items.add(fail("OMNI_SIGN_SECRET", String.join("；", signProblems) + "；改后需重建前端"));
            }
        }

        String admin = first(environment, "OMNI_ADMIN_INITIAL_PASSWORD", "omni.security.bootstrap-admin-password");
        if (!StringUtils.hasText(admin)) {
            items.add(fail("OMNI_ADMIN_INITIAL_PASSWORD",
                    "未设置（admin 登录密码；不是 admin123，也不是 OMNI_SIGN_SECRET）"));
        } else {
            String a = admin.trim();
            List<String> adminProblems = new ArrayList<>();
            if (a.length() < 12) {
                adminProblems.add("至少 12 位，当前=" + a.length());
            }
            if (FORBIDDEN.contains(a)) {
                adminProblems.add("不能使用演示密码 admin123/123456");
            }
            if (StringUtils.hasText(sign) && a.equals(sign.trim())) {
                adminProblems.add("不能与 OMNI_SIGN_SECRET 相同");
            }
            if (adminProblems.isEmpty()) {
                items.add(ok("OMNI_ADMIN_INITIAL_PASSWORD", "长度=" + a.length() + "（登录用此值，不是 SIGN）"));
            } else {
                items.add(fail("OMNI_ADMIN_INITIAL_PASSWORD", String.join("；", adminProblems)));
            }
        }

        String cors = first(environment, "OMNI_CORS_ORIGINS", "omni.security.cors.allowed-origin-patterns[0]");
        if (!StringUtils.hasText(cors)) {
            cors = environment.getProperty("omni.security.cors.allowed-origin-patterns");
        }
        if (!StringUtils.hasText(cors)) {
            items.add(fail("OMNI_CORS_ORIGINS", "未设置（例：https://your.domain），否则登录 403 CORS"));
        } else {
            items.add(ok("OMNI_CORS_ORIGINS", cors.trim()));
        }

        return items;
    }

    public static String formatReport(List<CheckItem> items) {
        long failCount = items.stream().filter(i -> !i.ok()).count();
        StringBuilder sb = new StringBuilder();
        sb.append('\n')
                .append("========== [prod] 部署配置检查（一次性列出全部项）==========\n");
        for (CheckItem item : items) {
            sb.append(item.ok() ? "[OK]   " : "[FAIL] ")
                    .append(item.name())
                    .append(" — ")
                    .append(item.detail())
                    .append('\n');
        }
        if (failCount == 0) {
            sb.append("结果：全部通过。登录账号 admin，密码 = OMNI_ADMIN_INITIAL_PASSWORD\n")
                    .append("禁止用 admin123 / OMNI_SIGN_SECRET 当登录密码。\n");
        } else {
            sb.append("结果：失败 ").append(failCount).append(" 项，已拒绝启动。请一次性改完后再发布。\n");
        }
        sb.append("==============================================================\n");
        return sb.toString();
    }

    private static boolean containsProd(String active) {
        if (!StringUtils.hasText(active)) {
            return false;
        }
        for (String profile : active.split(",")) {
            if ("prod".equalsIgnoreCase(profile.trim())) {
                return true;
            }
        }
        return false;
    }

    private static String first(Environment environment, String... keys) {
        if (environment == null) {
            return null;
        }
        for (String key : keys) {
            String v = environment.getProperty(key);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        // 环境变量兜底（部分阶段属性尚未绑定）
        for (String key : keys) {
            if (key.indexOf('.') >= 0) {
                continue;
            }
            String v = System.getenv(key);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }

    private static CheckItem ok(String name, String detail) {
        return new CheckItem(name, true, detail);
    }

    private static CheckItem fail(String name, String detail) {
        return new CheckItem(name, false, detail);
    }

    /**
     * 单条检查结果。
     */
    public record CheckItem(String name, boolean ok, String detail) {
    }
}
