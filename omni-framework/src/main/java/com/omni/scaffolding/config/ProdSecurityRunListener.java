package com.omni.scaffolding.config;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 生产配置硬校验（RunListener）：弥补 EnvironmentPostProcessor 阶段 profile 尚未激活的情况。
 */
public class ProdSecurityRunListener implements SpringApplicationRunListener, Ordered {

    private final String[] args;

    public ProdSecurityRunListener(SpringApplication application, String[] args) {
        this.args = args;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
                                    ConfigurableEnvironment environment) {
        if (!ProdDeployConfigChecker.isProd(environment, args)) {
            return;
        }
        ProdDeployConfigChecker.requireValidOrThrow(environment);
    }
}
