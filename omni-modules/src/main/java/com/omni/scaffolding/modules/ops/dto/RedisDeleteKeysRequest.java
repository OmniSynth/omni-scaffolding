package com.omni.scaffolding.modules.ops.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除 Key。
 */
@Data
public class RedisDeleteKeysRequest {

    /**
     * 待删除 Key 列表，必填。
     */
    @NotEmpty(message = "keys 不能为空")
    @Size(max = 100, message = "单次最多删除 100 个 Key")
    private List<@Size(max = 512, message = "Key 长度不能超过 512") String> keys;
}
