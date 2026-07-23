package com.omni.scaffolding.modules.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 写入 / 覆盖 String 类型 Key。
 */
@Data
public class RedisSetStringRequest {

    /** Key 名称，必填。 */
    @NotBlank(message = "Key 不能为空")
    @Size(max = 512, message = "Key 长度不能超过 512")
    private String key;

    /** 值，必填。 */
    @NotBlank(message = "值不能为空")
    @Size(max = 4000, message = "值长度不能超过 4000")
    private String value;

    /** 过期秒数；空或 &lt;=0 表示不过期。 */
    private Long ttlSeconds;
}
