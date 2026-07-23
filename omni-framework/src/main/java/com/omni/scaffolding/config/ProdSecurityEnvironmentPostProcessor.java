package com.omni.scaffolding.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 生产配置硬校验（Environment 阶段）：失败则一次性打印全部检查项。
 */
@Order(ConfigDataEnvironmentPostProcessor.ORDER + 30)
public class ProdSecurityEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 30;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!ProdDeployConfigChecker.isProd(environment)) {
            return;
        }
        ProdDeployConfigChecker.requireValidOrThrow(environment);
    }
}
