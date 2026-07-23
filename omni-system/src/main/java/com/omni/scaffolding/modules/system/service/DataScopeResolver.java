package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.modules.system.mapper.SysDeptQueryMapper;
import com.omni.scaffolding.security.AuthUser;
import com.omni.scaffolding.security.SecurityUtils;
import com.omni.scaffolding.security.datascope.DataScopeQuery;
import com.omni.scaffolding.security.datascope.DataScopeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据范围解析器。
 *
 * <p>从 {@link SecurityUtils} 读取当前登录用户的 {@code dataScope}/{@code deptId}，
 * 组装供 MyBatis 动态 SQL 使用的 {@link DataScopeQuery}；同时提供写操作越权校验。
 *
 * <p>范围语义：{@code ALL} &gt; {@code DEPT_AND_CHILD} &gt; {@code DEPT} &gt; {@code SELF}。
 */
@Component
@RequiredArgsConstructor
public class DataScopeResolver {

    private final SysDeptQueryMapper deptQueryMapper;

    /**
     * 解析当前用户的数据范围查询参数。
     *
     * <ul>
     *   <li>ALL：不过滤</li>
     *   <li>SELF：仅本人 userId</li>
     *   <li>DEPT：本部门</li>
     *   <li>DEPT_AND_CHILD：本部门及子孙（按 ancestors 展开）</li>
     * </ul>
     */
    public DataScopeQuery resolve() {
        AuthUser user = SecurityUtils.requireAuthUser();
        DataScopeType type = DataScopeType.from(user.getDataScope());
        if (type == DataScopeType.ALL) {
            return DataScopeQuery.all();
        }
        if (type == DataScopeType.SELF) {
            return new DataScopeQuery(type.name(), user.getId(), user.getDeptId(), List.of());
        }
        Long deptId = user.getDeptId();
        List<Long> deptIds;
        if (type == DataScopeType.DEPT) {
            deptIds = deptId == null ? List.of() : List.of(deptId);
        } else {
            deptIds = deptId == null ? List.of() : deptQueryMapper.findSelfAndChildIds(deptId);
        }
        return new DataScopeQuery(type.name(), user.getId(), deptId, deptIds);
    }

    /**
     * 判断目标用户是否在当前数据范围内（详情 / 写操作防越权）。
     */
    public boolean canAccessUser(Long targetUserId, Long targetDeptId) {
        DataScopeQuery ds = resolve();
        if (ds.isAll()) {
            return true;
        }
        DataScopeType type = DataScopeType.from(ds.getType());
        if (type == DataScopeType.SELF) {
            return SecurityUtils.requireUserId().equals(targetUserId);
        }
        return targetDeptId != null && ds.getDeptIds() != null && ds.getDeptIds().contains(targetDeptId);
    }
}
