package com.omni.scaffolding.security.datascope;

/**
 * 角色数据范围：多角色取最宽松。
 *
 * <p>优先级：{@link #ALL} &gt; {@link #DEPT_AND_CHILD} &gt; {@link #DEPT} &gt; {@link #SELF}。
 */
public enum DataScopeType {

    /**
     * 全部数据
     */
    ALL(4),
    /**
     * 本部门及以下
     */
    DEPT_AND_CHILD(3),
    /**
     * 仅本部门
     */
    DEPT(2),
    /**
     * 仅本人
     */
    SELF(1);

    /**
     * 宽松度排序权重，数值越大范围越宽。
     */
    private final int rank;

    DataScopeType(int rank) {
        this.rank = rank;
    }

    /**
     * @return 宽松度排序权重
     */
    public int getRank() {
        return rank;
    }

    /**
     * 从字符串解析；空或非法时回退 {@link #SELF}。
     *
     * @param value 范围编码，如 {@code ALL} / {@code DEPT}
     * @return 对应枚举值
     */
    public static DataScopeType from(String value) {
        if (value == null || value.isBlank()) {
            return SELF;
        }
        return DataScopeType.valueOf(value.trim().toUpperCase());
    }

    /**
     * 取更宽松的范围（多角色合并时使用）。
     *
     * @param a 范围 A
     * @param b 范围 B
     * @return 宽松度更高者；均为 null 时返回 {@link #SELF}
     */
    public static DataScopeType max(DataScopeType a, DataScopeType b) {
        if (a == null) {
            return b == null ? SELF : b;
        }
        if (b == null) {
            return a;
        }
        return a.rank >= b.rank ? a : b;
    }
}
