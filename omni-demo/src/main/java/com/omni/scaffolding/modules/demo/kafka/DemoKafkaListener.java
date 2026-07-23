package com.omni.scaffolding.modules.demo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 演示消费者：仅 {@code omni.kafka.enabled=true} 时注册。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "omni.kafka", name = "enabled", havingValue = "true")
public class DemoKafkaListener {

    /**
     * 消费演示 Topic 消息并打日志（验证端到端链路）。
     *
     * @param payload   消息体
     * @param topic     来源 Topic
     * @param partition 分区号
     * @param offset    消费位点
     */
    @KafkaListener(
            topics = "${omni.kafka.demo-topic}",
            groupId = "${spring.kafka.consumer.group-id:omni-scaffolding}"
    )
    public void onMessage(@Payload String payload,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                          @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Kafka consumed topic={}, partition={}, offset={}, payload={}",
                topic, partition, offset, payload);
    }
}
