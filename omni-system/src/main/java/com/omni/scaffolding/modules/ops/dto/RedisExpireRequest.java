package com.omni.scaffolding.modules.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 设置 / 清除 TTL。
 */
@Data
public class RedisExpireRequest {

    /** Key 名称，必填。 */
    @NotBlank(message = "Key 不能为空")
    @Size(max = 512, message = "Key 长度不能超过 512")
    private String key;

    /**
     * 过期秒数；{@code null} 或 &lt;=0 表示持久化（移除过期）。
     */
    @NotNull(message = "ttlSeconds 不能为空，传 0 表示永不过期")
    private Long ttlSeconds;
}
