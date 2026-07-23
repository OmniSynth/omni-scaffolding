package com.omni.scaffolding.modules.demo.kafka;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 演示消息发送请求。
 */
@Data
public class DemoKafkaPublishRequest {

    /**
     * 消息 Key，可选；相同 Key 进入同一分区。
     */
    private String key;

    /**
     * 事件类型。
     */
    @NotBlank
    private String type;

    /**
     * 事件内容。
     */
    @NotBlank
    private String message;
}
