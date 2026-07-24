package com.omni.scaffolding.modules.open.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 创建 / 修改开放 API 客户端请求。
 */
@Data
public class OpenClientSaveRequest {

    /**
     * 客户端名称。
     */
    @NotBlank(message = "客户端名称不能为空")
    @Size(max = 128, message = "客户端名称长度不能超过 128")
    private String name;

    /**
     * 日调用上限；{@code null}/≤0 表示不限制。
     */
    private Integer dailyLimit;

    /**
     * QPS；{@code null}/≤0 表示不限制。
     */
    private Integer qpsLimit;

    /**
     * 过期时间；空表示不过期。
     */
    private Instant expireAt;

    /**
     * 备注。
     */
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status;

    /**
     * IP 白名单；空表示不限制。
     */
    private List<String> ipList = new ArrayList<>();

    /**
     * 可访问的开放接口 ID 列表。
     */
    private List<Long> endpointIds = new ArrayList<>();
}
