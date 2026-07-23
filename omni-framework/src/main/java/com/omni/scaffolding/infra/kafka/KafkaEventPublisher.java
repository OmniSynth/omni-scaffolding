package com.omni.scaffolding.infra.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 消息发送封装（仅 Kafka 启用时注册）。
 *
 * <p>统一用 String Key + JSON Value，业务侧传 POJO 即可。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "omni.kafka", name = "enabled", havingValue = "true")
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 异步发送；返回 CompletableFuture 便于调用方按需等待或回调。
     *
     * @param topic   目标 Topic
     * @param key     消息 Key（分区 / 顺序）
     * @param payload POJO 或 JSON 字符串
     * @return 发送结果的 Future
     */
    public CompletableFuture<SendResult<String, String>> publish(String topic, String key, Object payload) {
        String json = toJson(payload);
        log.debug("Kafka send topic={}, key={}, payload={}", topic, key, json);
        return kafkaTemplate.send(topic, key, json);
    }

    /**
     * 同步等待发送结果（演示 / 强一致场景）；超时由 producer 配置控制。
     *
     * @param topic   目标 Topic
     * @param key     消息 Key
     * @param payload POJO 或 JSON 字符串
     */
    public void publishSync(String topic, String key, Object payload) {
        try {
            publish(topic, key, payload).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Kafka 发送被中断");
        } catch (Exception ex) {
            Throwable root = rootCause(ex);
            String detail = root.getClass().getSimpleName() + ": " + root.getMessage();
            log.error("Kafka publish failed topic={}, key={}, cause={}", topic, key, detail, ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Kafka 发送失败: " + detail);
        }
    }

    /**
     * 沿 cause 链查找根异常。
     *
     * @param ex 原始异常
     * @return 最内层 cause，无则返回自身
     */
    private static Throwable rootCause(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    /**
     * 将消息载荷序列化为 JSON 字符串；已是字符串则原样返回。
     *
     * @param payload POJO 或 JSON 字符串
     * @return Kafka Value 文本
     */
    private String toJson(Object payload) {
        try {
            if (payload instanceof String str) {
                return str;
            }
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Kafka 消息序列化失败");
        }
    }
}
