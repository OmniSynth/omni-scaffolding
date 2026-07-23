package com.omni.scaffolding.config;

import com.omni.scaffolding.modules.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 按配置/环境变量初始化 admin 登录密码（部署可自定义）。
 *
 * <p>读取 {@code omni.security.bootstrap-admin-password}（通常由
 * {@code OMNI_ADMIN_INITIAL_PASSWORD} 注入）。未配置则跳过，保留 Flyway V1 种子
 * （明文 {@code admin123}）。
 *
 * <p>仅当库中密码仍为演示口令时替换（{@code admin123} / 历史错误种子 {@code 123456}），
 * 避免覆盖管理员自行改密。须在 {@link AdminPasswordInitializer} 之后执行。
 *
 * <p>密码不合规时走 {@link ProdDeployConfigChecker}（清单只打印一次）。
 */
@Slf4j
@Component
@Order(AdminPasswordBootstrapInitializer.ORDER)
@RequiredArgsConstructor
public class AdminPasswordBootstrapInitializer implements ApplicationRunner {

    /** 晚于 {@link AdminPasswordInitializer}，避免被改回 admin123 */
    public static final int ORDER = 200;

    /** 与 V1 种子及历史错误种子对应的明文 */
    private static final List<String> DEMO_PASSWORDS = List.of("admin123", "123456");

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OmniSecurityProperties securityProperties;
    private final Environment environment;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String initialPassword = securityProperties.getBootstrapAdminPassword();
        if (!StringUtils.hasText(initialPassword)) {
            log.debug("omni.security.bootstrap-admin-password 未配置，跳过 admin 密码初始化");
            return;
        }
        initialPassword = initialPassword.trim();
        if (initialPassword.length() < 12 || DEMO_PASSWORDS.contains(initialPassword)) {
            // 清单只由 Checker 打印一次；异常用短消息，避免与 Spring 堆栈重复
            ProdDeployConfigChecker.requireValidOrThrow(environment);
            throw new IllegalStateException(
                    "OMNI_ADMIN_INITIAL_PASSWORD 不合规（至少 12 位，且不能为 admin123/123456）");
        }

        String passwordToSet = initialPassword;
        userRepository.findByUsernameAndDeleted("admin", 0).ifPresentOrElse(admin -> {
            boolean stillDemo = DEMO_PASSWORDS.stream()
                    .anyMatch(demo -> passwordEncoder.matches(demo, admin.getPasswordHash()));
            if (!stillDemo) {
                log.warn("""

                        ========== admin 密码未改动 ==========
                        库中 admin 已不是演示口令，跳过 OMNI_ADMIN_INITIAL_PASSWORD 写入。
                        登录请仍用「上次成功初始化时」的 OMNI_ADMIN_INITIAL_PASSWORD，
                        或由已登录管理员在系统里重置；不要用 admin123 / OMNI_SIGN_SECRET。
                        ======================================
                        """);
                return;
            }
            String encoded = passwordEncoder.encode(passwordToSet);
            admin.setPasswordHash(encoded);
            userRepository.saveAndFlush(admin);
            if (!passwordEncoder.matches(passwordToSet, encoded)) {
                throw new IllegalStateException("admin 密码 bootstrap 自检失败：编码结果与明文不匹配");
            }
            log.warn("""

                    ========== admin 密码已初始化 ==========
                    用户名           : admin
                    密码来源         : 环境变量 OMNI_ADMIN_INITIAL_PASSWORD（明文长度={}）
                    请用上述环境变量的值登录
                    禁止使用         : admin123 / 123456 / OMNI_SIGN_SECRET
                    登录后请尽快改密
                    ========================================
                    """, passwordToSet.length());
        }, () -> log.error("""

                ========== [部署异常] 未找到 admin 用户 ==========
                库中无 username=admin 且 deleted=0 的账号，无法完成密码 bootstrap。
                请检查 Flyway 是否执行、DB_NAME 是否连对。
                ================================================
                """));
    }
}
