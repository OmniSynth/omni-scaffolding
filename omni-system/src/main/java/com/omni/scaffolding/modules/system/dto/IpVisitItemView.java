package com.omni.scaffolding.modules.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个 IP 的今日访问次数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpVisitItemView {

    /**
     * 客户端 IP。
     */
    private String ip;

    /**
     * 当日访问次数。
     */
    private long count;
}
