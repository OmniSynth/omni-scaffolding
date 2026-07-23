package com.omni.scaffolding.config;

import com.omni.scaffolding.modules.system.entity.SysUser;
import com.omni.scaffolding.modules.system.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 开发便利：确保演示账号密码与文档一致（{@code admin123}）。
 *
 * <p>仅在哈希不匹配时回写。生产环境建议关闭或改为强制改密流程。
 */
@Slf4j
@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class AdminPasswordInitializer implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "admin123";
    private static final List<String> DEMO_USERS = List.of("admin", "rd_mgr", "sales1", "rd_dev");

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String username : DEMO_USERS) {
            userRepository.findByUsernameAndDeleted(username, 0).ifPresent(this::ensurePassword);
        }
    }

    private void ensurePassword(SysUser user) {
        if (!passwordEncoder.matches(DEMO_PASSWORD, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            userRepository.save(user);
            log.info("Initialized default password for user {}", user.getUsername());
        }
    }
}
