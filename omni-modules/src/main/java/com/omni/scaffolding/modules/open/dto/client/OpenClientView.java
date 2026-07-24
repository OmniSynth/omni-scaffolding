package com.omni.scaffolding.modules.open.dto.client;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 开放 API 客户端读模型（不含明文密钥）。
 */
@Data
public class OpenClientView {

    /**
     * 主键。
     */
    private Long id;

    /**
     * 客户端名称。
     */
    private String name;

    /**
     * 公开 AccessKey（可展示）。
     */
    private String accessKey;

    /**
     * 日调用上限；空表示不限。
     */
    private Integer dailyLimit;

    /**
     * QPS 上限；空表示不限。
     */
    private Integer qpsLimit;

    /**
     * 过期时间；空表示不过期。
     */
    private Instant expireAt;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 是否启用。
     */
    private Boolean status;

    /**
     * IP 白名单；空列表表示不限制 IP。
     */
    private List<String> ipList = new ArrayList<>();

    /**
     * 已绑定的开放接口 ID 列表。
     */
    private List<Long> endpointIds = new ArrayList<>();

    /**
     * 当日已调用次数（来自 Redis）。
     */
    private Long todayUsed;
}
