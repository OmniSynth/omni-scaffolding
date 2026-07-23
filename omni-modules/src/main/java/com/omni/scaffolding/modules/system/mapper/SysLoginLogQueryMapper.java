package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.log.LoginLogView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志查询 Mapper。
 *
 * <p>对应 XML：{@code classpath:mapper/system/SysLoginLogQueryMapper.xml}。
 */
@Mapper
public interface SysLoginLogQueryMapper {

    /**
     * 条件统计登录日志条数。
     *
     * @param username 可选，匹配用户名
     * @param status   可选，SUCCESS / FAIL
     * @param ip       可选，匹配 IP
     * @return 总数
     */
    long count(@Param("username") String username,
               @Param("status") String status,
               @Param("ip") String ip);

    /**
     * 条件分页搜索登录日志，按时间倒序。
     *
     * @param username 可选，匹配用户名
     * @param status   可选，SUCCESS / FAIL
     * @param ip       可选，匹配 IP
     * @param limit    每页条数
     * @param offset   偏移量
     * @return 读模型列表
     */
    List<LoginLogView> search(@Param("username") String username,
                              @Param("status") String status,
                              @Param("ip") String ip,
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
