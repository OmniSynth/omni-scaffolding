package com.omni.scaffolding.modules.ops.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Redis 运行概览。
 */
@Data
public class RedisInfoView {

    /**
     * Redis 版本。
     */
    private String redisVersion;

    /**
     * 运行模式（standalone / cluster 等）。
     */
    private String mode;

    /**
     * 操作系统。
     */
    private String os;

    /**
     * 运行时长（秒）。
     */
    private Long uptimeInSeconds;

    /**
     * 已连接客户端数。
     */
    private Long connectedClients;

    /**
     * 已用内存（字节）。
     */
    private Long usedMemory;

    /**
     * 已用内存（人类可读）。
     */
    private String usedMemoryHuman;

    /**
     * 最大内存限制（字节）。
     */
    private Long maxMemory;

    /**
     * 最大内存限制（人类可读）。
     */
    private String maxMemoryHuman;

    /**
     * 当前库 Key 总数。
     */
    private Long totalKeys;

    /**
     * 瞬时 OPS。
     */
    private Long instantaneousOpsPerSec;

    /**
     * 角色（master / slave）。
     */
    private String role;

    /**
     * 额外关键指标，便于排查。
     */
    private Map<String, String> extras = new LinkedHashMap<>();
}
