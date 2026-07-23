package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 字典类型 / 数据启停请求。
 */
@Data
public class DictStatusRequest {

    /** 是否启用。 */
    @NotNull(message = "状态不能为空")
    private Boolean status;
}
