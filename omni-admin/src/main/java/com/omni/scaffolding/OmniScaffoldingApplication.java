package com.omni.scaffolding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 应用启动入口。
 *
 * <p>{@link EnableJpaAuditing} 启用 JPA 审计，配合 {@code BaseAuditableEntity}
 * 自动填充 {@code createdAt / updatedAt}。
 *
 * <p>异步与缓存开关见 {@code config.AsyncConfig}；虚拟线程见 {@code config.VirtualThreadConfig}。
 */
@EnableJpaAuditing
@SpringBootApplication
public class OmniScaffoldingApplication {

    /**
     * 应用启动入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OmniScaffoldingApplication.class, args);
    }
}
