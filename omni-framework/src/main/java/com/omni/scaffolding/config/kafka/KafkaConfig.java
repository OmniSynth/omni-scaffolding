package com.omni.scaffolding.config.kafka;

import com.omni.scaffolding.config.OmniKafkaProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 装配（仅 {@code omni.kafka.enabled=true} 时生效）。
 *
 * <p>连接、序列化等通用项使用 Spring Boot {@code spring.kafka.*}；
 * 脚手架额外约定见 {@link OmniKafkaProperties}。
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(prefix = "omni.kafka", name = "enabled", havingValue = "true")
public class KafkaConfig {

    /**
     * 自动创建演示 Topic（Broker 允许 auto.create 或具备建 Topic 权限时生效）。
     *
     * <p>可通过 {@code omni.kafka.auto-create-topic=false} 关闭（例如外部 Broker 无建 Topic 权限时）。
     */
    @Bean
    @ConditionalOnProperty(prefix = "omni.kafka", name = "auto-create-topic", havingValue = "true", matchIfMissing = true)
    public NewTopic omniDemoTopic(OmniKafkaProperties properties) {
        return TopicBuilder.name(properties.getDemoTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
