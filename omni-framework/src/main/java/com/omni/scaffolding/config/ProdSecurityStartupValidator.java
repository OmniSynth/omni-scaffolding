package com.omni.scaffolding.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * prod 容器刷新阶段校验：在打印 {@code Started} 之前失败，避免先“启动成功”再因配置退出。
 */
@Configuration
@Profile("prod")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ProdSecurityStartupValidator {

    private final Environment environment;

    @PostConstruct
    public void validate() {
        ProdDeployConfigChecker.requireValidOrThrow(environment);
    }
}
