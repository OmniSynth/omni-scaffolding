package com.omni.scaffolding.modules.ops.dto;

import lombok.Data;

/**
 * Redis Key 列表项。
 */
@Data
public class RedisKeyView {

    /**
     * Key 名称。
     */
    private String key;

    /**
     * 类型：string / hash / list / set / zset / none / unknown。
     */
    private String type;

    /**
     * TTL 秒：-1 永不过期，-2 不存在。
     */
    private Long ttlSeconds;
}
