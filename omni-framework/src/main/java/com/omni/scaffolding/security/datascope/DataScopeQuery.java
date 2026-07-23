package com.omni.scaffolding.security.datascope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * MyBatis 数据范围参数：由 {@link DataScopeResolver} 根据当前登录用户组装。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeQuery {

    /**
     * 范围类型编码：ALL / SELF / DEPT / DEPT_AND_CHILD
     */
    private String type;

    /**
     * 当前用户 ID（SELF）
     */
    private Long userId;

    /**
     * 当前部门 ID
     */
    private Long deptId;

    /**
     * 可见部门 ID 列表（DEPT / DEPT_AND_CHILD）
     */
    private List<Long> deptIds = new ArrayList<>();

    /**
     * 构造「全部数据」范围，MyBatis 侧不加部门 / 用户过滤。
     *
     * @return 类型为 {@link DataScopeType#ALL} 的查询参数
     */
    public static DataScopeQuery all() {
        return new DataScopeQuery(DataScopeType.ALL.name(), null, null, List.of());
    }

    /**
     * 是否为全部数据范围。
     *
     * @return {@code true} 时 type 为 {@link DataScopeType#ALL}
     */
    public boolean isAll() {
        return DataScopeType.ALL.name().equals(type);
    }
}
