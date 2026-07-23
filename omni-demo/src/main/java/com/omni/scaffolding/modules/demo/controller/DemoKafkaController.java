package com.omni.scaffolding.modules.demo.controller;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.modules.demo.kafka.DemoKafkaEvent;
import com.omni.scaffolding.modules.demo.kafka.DemoKafkaPublishRequest;
import com.omni.scaffolding.modules.demo.service.DemoKafkaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kafka 演示 API：仅启用 Kafka 时注册该 Controller。
 */
@Tag(name = "Demo Kafka")
@RestController
@RequestMapping("/api/demo/kafka")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "omni.kafka", name = "enabled", havingValue = "true")
public class DemoKafkaController {

    private final DemoKafkaService demoKafkaService;

    /**
     * 发送演示消息到 Kafka Topic。
     *
     * @param request 消息类型、内容与可选分区键
     * @return 已发送的事件（含时间戳）
     */
    @Operation(summary = "发送演示消息到 Kafka")
    @PostMapping("/publish")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DemoKafkaEvent> publish(@Valid @RequestBody DemoKafkaPublishRequest request) {
        return ApiResponse.ok(demoKafkaService.publish(request));
    }
}
