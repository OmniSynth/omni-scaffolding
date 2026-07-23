package com.omni.scaffolding.modules.demo.service;

import com.omni.scaffolding.config.OmniKafkaProperties;
import com.omni.scaffolding.infra.kafka.KafkaEventPublisher;
import com.omni.scaffolding.modules.demo.kafka.DemoKafkaEvent;
import com.omni.scaffolding.modules.demo.kafka.DemoKafkaPublishRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Kafka 演示业务：封装 Topic 与事件构造。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "omni.kafka", name = "enabled", havingValue = "true")
public class DemoKafkaService {

    private final KafkaEventPublisher publisher;
    private final OmniKafkaProperties kafkaProperties;

    /**
     * 构造演示事件并同步发送到 Kafka。
     *
     * @param request 消息类型、内容与可选分区键
     * @return 已发送的事件（含时间戳）
     */
    public DemoKafkaEvent publish(DemoKafkaPublishRequest request) {
        DemoKafkaEvent event = new DemoKafkaEvent(request.getType(), request.getMessage(), Instant.now());
        String key = (request.getKey() == null || request.getKey().isBlank())
                ? request.getType()
                : request.getKey();
        publisher.publishSync(kafkaProperties.getDemoTopic(), key, event);
        return event;
    }
}
