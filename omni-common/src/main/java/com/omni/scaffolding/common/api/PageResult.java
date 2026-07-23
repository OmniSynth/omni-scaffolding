package com.omni.scaffolding.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一分页结果结构，供列表接口复用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 当前页码（从 1 开始）。
     */
    private long page;

    /**
     * 每页大小。
     */
    private long size;

    /**
     * 总记录数。
     */
    private long total;

    /**
     * 当前页数据。
     */
    private List<T> records;

    /**
     * 构造分页结果。
     *
     * @param page    当前页码（从 1 开始）
     * @param size    每页大小
     * @param total   总记录数
     * @param records 当前页数据
     * @param <T>     元素类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(long page, long size, long total, List<T> records) {
        return new PageResult<>(page, size, total, records);
    }
}
