package com.omni.scaffolding.modules.system.mapper;

import com.omni.scaffolding.modules.system.dto.job.JobLogView;
import com.omni.scaffolding.modules.system.dto.job.JobView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 定时任务读模型 Mapper（MyBatis）。
 *
 * <p>管理端任务列表 / 详情及执行日志分页查询。
 */
@Mapper
public interface SysJobQueryMapper {

    /**
     * 统计符合条件的未删除任务数。
     *
     * @param keyword 可选，匹配任务名称 / 调用目标
     * @param status  可选，启停状态
     * @return 总数
     */
    long countJobs(@Param("keyword") String keyword, @Param("status") Boolean status);

    /**
     * 分页查询未删除任务。
     *
     * @param keyword 可选，匹配任务名称 / 调用目标
     * @param status  可选，启停状态
     * @param limit   每页条数
     * @param offset  偏移量
     * @return 读模型列表
     */
    List<JobView> listJobs(@Param("keyword") String keyword,
                           @Param("status") Boolean status,
                           @Param("limit") long limit,
                           @Param("offset") long offset);

    /**
     * 按主键查询未删除任务详情。
     *
     * @param jobId 任务主键
     * @return 读模型，不存在则为 {@code null}
     */
    JobView findById(@Param("jobId") Long jobId);

    /**
     * 统计指定任务的执行日志数。
     *
     * @param jobId 任务主键
     * @return 总数
     */
    long countLogs(@Param("jobId") Long jobId);

    /**
     * 分页查询指定任务的执行日志。
     *
     * @param jobId  任务主键
     * @param limit  每页条数
     * @param offset 偏移量
     * @return 日志读模型列表
     */
    List<JobLogView> listLogs(@Param("jobId") Long jobId,
                              @Param("limit") long limit,
                              @Param("offset") long offset);
}
