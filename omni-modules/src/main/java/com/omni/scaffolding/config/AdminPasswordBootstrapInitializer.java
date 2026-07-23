package com.omni.scaffolding.config;

import com.omni.scaffolding.modules.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 按配置/环境变量初始化 admin 登录密码（部署可自定义）。
 *
 * <p>读取 {@code omni.security.bootstrap-admin-password}（通常由
 * {@code OMNI_ADMIN_INITIAL_PASSWORD} 注入）。未配置则跳过。
 *
 * <p>仅当库中密码仍为演示口令时替换（含历史错误种子 {@code 123456}），
 * 避免覆盖管理员自行改密。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminPasswordBootstrapInitializer implements ApplicationRunner {

    /** 文档约定演示密码；以及 V1 历史错误种子对应明文 */
    private static final List<String> DEMO_PASSWORDS = List.of("admin123", "123456");

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OmniSecurityProperties securityProperties;

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
            throw new IllegalStateException(
                    "OMNI_ADMIN_INITIAL_PASSWORD 至少 12 位，且不能使用演示密码 admin123 / 123456");
        }

        String passwordToSet = initialPassword;
        userRepository.findByUsernameAndDeleted("admin", 0).ifPresentOrElse(admin -> {
            boolean stillDemo = DEMO_PASSWORDS.stream()
                    .anyMatch(demo -> passwordEncoder.matches(demo, admin.getPasswordHash()));
            if (!stillDemo) {
                log.info("Admin 密码已非演示口令，跳过 bootstrap");
                return;
            }
            String encoded = passwordEncoder.encode(passwordToSet);
            admin.setPasswordHash(encoded);
            // 同事务内立刻可被后续读路径看到；避免仅 save 未 flush 的边界情况
            userRepository.saveAndFlush(admin);
            if (!passwordEncoder.matches(passwordToSet, encoded)) {
                throw new IllegalStateException("admin 密码 bootstrap 自检失败：编码结果与明文不匹配");
            }
            log.warn(
                    "已将演示 admin 密码替换为 OMNI_ADMIN_INITIAL_PASSWORD（长度={}），请用该环境变量登录，不要用页面默认的 admin123",
                    passwordToSet.length());
        }, () -> log.warn("未找到 admin 用户，跳过密码 bootstrap"));
    }
}
