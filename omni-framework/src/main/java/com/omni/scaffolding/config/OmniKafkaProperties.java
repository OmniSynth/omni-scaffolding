package com.omni.scaffolding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Kafka 开关与脚手架约定配置。
 *
 * <p>默认 {@code enabled=false}：不连接 Broker，也不加载监听器；
 * 需要时设 {@code omni.kafka.enabled=true} 并配置 {@code spring.kafka.bootstrap-servers}。
 */
@Data
@Component
@ConfigurationProperties(prefix = "omni.kafka")
public class OmniKafkaProperties {

    /**
     * 是否启用 Kafka。
     * <p>false（默认）时由 {@link com.omni.scaffolding.config.kafka.KafkaEnableEnvironmentPostProcessor}
     * 排除 {@code KafkaAutoConfiguration}，应用可无 Kafka 正常启动。
     */
    private boolean enabled = false;

    /**
     * 演示 Topic，示例生产者 / 消费者共用。
     */
    private String demoTopic = "omni.demo.events";

    /**
     * 是否由脚手架自动声明演示 Topic（{@link org.apache.kafka.clients.admin.NewTopic}）。
     * <p>外部集群无建 Topic 权限时设为 false。
     */
    private boolean autoCreateTopic = true;
}
