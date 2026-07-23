package com.omni.scaffolding.modules.demo.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 演示用 Kafka 事件载荷。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemoKafkaEvent {

    /**
     * 事件类型，如 PRODUCT_CREATED。
     */
    private String type;

    /**
     * 业务内容（任意文本）。
     */
    private String message;

    /**
     * 发送时间。
     */
    private Instant sentAt;
}
