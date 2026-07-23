package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.log.OperLogView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志查询 Mapper。
 *
 * <p>对应 XML：{@code classpath:mapper/system/SysOperLogQueryMapper.xml}。
 */
@Mapper
public interface SysOperLogQueryMapper {

    /**
     * 条件统计操作日志条数。
     *
     * @param username 可选，匹配操作人
     * @param module   可选，匹配模块名
     * @param status   可选，SUCCESS / FAIL
     * @return 总数
     */
    long count(@Param("username") String username,
               @Param("module") String module,
               @Param("status") String status);

    /**
     * 条件分页搜索操作日志，按时间倒序。
     *
     * @param username 可选，匹配操作人
     * @param module   可选，匹配模块名
     * @param status   可选，SUCCESS / FAIL
     * @param limit    每页条数
     * @param offset   偏移量
     * @return 读模型列表
     */
    List<OperLogView> search(@Param("username") String username,
                             @Param("module") String module,
                             @Param("status") String status,
                             @Param("limit") long limit,
                             @Param("offset") long offset);

    /**
     * 按主键删除。
     *
     * @param id 日志主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
