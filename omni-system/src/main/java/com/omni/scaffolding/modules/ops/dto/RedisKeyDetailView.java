package com.omni.scaffolding.modules.ops.dto;

import lombok.Data;

/**
 * Redis Key 详情（大 value 会截断）。
 */
@Data
public class RedisKeyDetailView {

    /**
     * Key 名称。
     */
    private String key;

    /**
     * 类型。
     */
    private String type;

    /**
     * TTL 秒。
     */
    private Long ttlSeconds;

    /**
     * 元素数量或字符串长度。
     */
    private Long size;

    /**
     * 字符串值，或结构化数据的 JSON 文本。
     */
    private String value;

    /**
     * 值是否被截断。
     */
    private boolean truncated;
}
