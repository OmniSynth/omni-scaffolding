package com.omni.scaffolding.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * prod 启动配置校验：在 Logback 就绪后执行，把全部 [FAIL] 打到控制台和日志文件，再拒绝启动。
 *
 * <p>运维/初学者看 Jenkins 控制台或 {@code omni-scaffolding.log} 即可定位，无需先改 shell。
 */
@Slf4j
@Configuration
@Profile("prod")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ProdSecurityStartupValidator {

    private final Environment environment;

    @PostConstruct
    public void validate() {
        log.warn("开始执行 prod 部署配置检查（不通过将拒绝启动，清单见后续 ERROR）…");
        ProdDeployConfigChecker.requireValidOrThrow(environment);
    }
}
