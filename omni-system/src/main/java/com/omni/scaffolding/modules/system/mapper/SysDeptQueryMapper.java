package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.DeptView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门复杂读 Mapper（MyBatis）。
 *
 * <p>对应 XML：{@code classpath:mapper/system/SysDeptQueryMapper.xml}。
 * 部门主表写入走 JPA（{@code SysDeptRepository}）；本接口返回扁平列表（含直属用户数），由服务层组树。
 */
@Mapper
public interface SysDeptQueryMapper {

    /**
     * 全部未删除部门（扁平列表，含 {@code userCount}），服务层组树并按数据范围裁剪。
     */
    List<DeptView> listAll();

    /**
     * 本部门及全部子孙部门 ID。
     *
     * <p>匹配规则：{@code id = deptId}，或 {@code ancestors} 路径包含该部门
     * （MySQL/H2 通用的 {@code CONCAT + LIKE} 写法）。
     *
     * @param deptId 起始部门主键
     */
    List<Long> findSelfAndChildIds(@Param("deptId") Long deptId);
}
