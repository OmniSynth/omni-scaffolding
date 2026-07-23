package com.omni.scaffolding.config;

import com.omni.scaffolding.modules.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 生产环境首次启动时替换迁移脚本中的演示管理员密码。
 *
 * <p>仅当当前密码仍为 {@code admin123} 时更新，因此管理员后续自行改密不会在重启时被覆盖。
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProductionAdminPasswordInitializer implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "admin123";
    private static final String PASSWORD_PROPERTY = "omni.security.bootstrap-admin-password";

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String initialPassword = environment.getRequiredProperty(PASSWORD_PROPERTY);
        if (initialPassword.length() < 12 || DEMO_PASSWORD.equals(initialPassword)) {
            throw new IllegalStateException("OMNI_ADMIN_INITIAL_PASSWORD 至少 12 位且不能使用演示密码");
        }

        userRepository.findByUsernameAndDeleted("admin", 0).ifPresent(admin -> {
            if (passwordEncoder.matches(DEMO_PASSWORD, admin.getPasswordHash())) {
                admin.setPasswordHash(passwordEncoder.encode(initialPassword));
                userRepository.save(admin);
                log.warn("Replaced the default production admin password; rotate it after first login");
            }
        });
    }
}
