package com.omni.scaffolding.modules.system.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 白名单接口「今日访问」汇总视图。
 *
 * <p>数据来自 Redis 计数（见 {@link com.omni.scaffolding.common.cache.RedisKeys}）。
 */
@Data
public class IpVisitTodayView {

    /**
     * 统计日期，格式 {@code yyyyMMdd}。
     */
    private String date;

    /**
     * 当日访问总次数。
     */
    private long total;

    /**
     * 按 IP 拆分的访问明细（次数降序）。
     */
    private List<IpVisitItemView> items = new ArrayList<>();
}
