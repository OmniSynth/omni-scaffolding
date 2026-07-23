package com.omni.scaffolding.config;

import com.omni.scaffolding.modules.system.entity.SysUser;
import com.omni.scaffolding.modules.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 开发便利：确保演示账号密码与 Flyway V1 种子一致（明文 {@code admin123}）。
 *
 * <p>在 {@link AdminPasswordBootstrapInitializer} 之前执行。若已配置
 * {@code omni.security.bootstrap-admin-password}，则<strong>不改写 admin</strong>，
 * 避免把 SQL 种子 → bootstrap 替换 再强制改回 admin123 的互相覆盖。
 */
@Slf4j
@Component
@Profile({"dev", "test"})
@Order(AdminPasswordInitializer.ORDER)
@RequiredArgsConstructor
public class AdminPasswordInitializer implements ApplicationRunner {

    /** 先于 {@link AdminPasswordBootstrapInitializer} */
    public static final int ORDER = 100;

    /** 与 V1__init_schema.sql 中 password_hash 对应的明文 */
    private static final String DEMO_PASSWORD = "admin123";

    private static final List<String> DEMO_USERS = List.of("admin", "rd_mgr", "sales1", "rd_dev");

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OmniSecurityProperties securityProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean bootstrapAdmin = StringUtils.hasText(securityProperties.getBootstrapAdminPassword());
        for (String username : DEMO_USERS) {
            if (bootstrapAdmin && "admin".equals(username)) {
                log.debug("已配置 bootstrap-admin-password，跳过 admin（交由 AdminPasswordBootstrapInitializer）");
                continue;
            }
            userRepository.findByUsernameAndDeleted(username, 0).ifPresent(this::ensurePassword);
        }
    }

    private void ensurePassword(SysUser user) {
        if (!passwordEncoder.matches(DEMO_PASSWORD, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            userRepository.saveAndFlush(user);
            log.info("已将用户 {} 密码对齐为 Flyway 演示口令 admin123", user.getUsername());
        }
    }
}
