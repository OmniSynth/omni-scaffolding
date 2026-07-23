package com.omni.scaffolding.common.api;

/**
 * 分页查询参数规范化工具。
 *
 * <p>页码从 1 开始，默认每页 10 条，单页最大 200。
 */
public final class PageQuery {

    /**
     * 默认页码（从 1 开始）。
     */
    public static final long DEFAULT_PAGE = 1L;

    /**
     * 默认每页条数。
     */
    public static final long DEFAULT_SIZE = 10L;

    /**
     * 单页最大条数上限。
     */
    public static final long MAX_SIZE = 200L;

    private final long page;
    private final long size;

    /**
     * 私有构造，通过 {@link #of(Long, Long)} 创建实例。
     *
     * @param page 页码
     * @param size 每页条数
     */
    private PageQuery(long page, long size) {
        this.page = page;
        this.size = size;
    }

    /**
     * 规范化分页参数。
     *
     * @param page 原始页码，null/&lt;1 时视为 1
     * @param size 原始每页条数，null/&lt;1 时视为 10，超过 {@link #MAX_SIZE} 时截断
     * @return 规范化后的分页参数
     */
    public static PageQuery of(Long page, Long size) {
        long p = page == null || page < 1 ? DEFAULT_PAGE : page;
        long s = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new PageQuery(p, s);
    }

    /**
     * @return 当前页码（从 1 开始）
     */
    public long getPage() {
        return page;
    }

    /**
     * @return 每页条数
     */
    public long getSize() {
        return size;
    }

    /**
     * SQL OFFSET，从 0 开始。
     *
     * @return {@code (page - 1) * size}
     */
    public long getOffset() {
        return (page - 1) * size;
    }

    /**
     * 组装分页结果。
     *
     * @param total   总记录数
     * @param records 当前页数据
     * @param <T>     元素类型
     * @return 分页结果
     */
    public <T> PageResult<T> toResult(long total, java.util.List<T> records) {
        return PageResult.of(page, size, total, records);
    }
}
