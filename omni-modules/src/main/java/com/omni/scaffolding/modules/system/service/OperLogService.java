package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.trace.TraceContext;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.dto.log.OperLogView;
import com.omni.scaffolding.modules.system.entity.SysOperLog;
import com.omni.scaffolding.modules.system.mapper.SysOperLogQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysOperLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 操作日志服务。
 *
 * <p>由 {@link com.omni.scaffolding.modules.system.audit.OperLogAspect} 调用写入；查询供管理端使用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperLogService {

    private final SysOperLogRepository operLogRepository;
    private final SysOperLogQueryMapper operLogQueryMapper;

    /**
     * 独立事务写入一条操作日志。
     *
     * @param row 日志实体
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(SysOperLog row) {
        try {
            if (row.getId() == null) {
                row.setId(IdGenerator.nextId());
            }
            if (row.getOperTime() == null) {
                row.setOperTime(Instant.now());
            }
            if (row.getTraceId() == null) {
                row.setTraceId(TraceContext.getTraceId());
            }
            operLogRepository.save(row);
        } catch (Exception ex) {
            log.warn("failed to persist oper log: {}", ex.getMessage());
        }
    }

    /**
     * 条件分页查询操作日志。
     *
     * @param username 可选，匹配操作人
     * @param module   可选，匹配模块名
     * @param status   可选，SUCCESS / FAIL
     * @param page     页码
     * @param size     每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<OperLogView> search(String username, String module, String status, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        String u = blankToNull(username);
        String m = blankToNull(module);
        String s = blankToNull(status);
        long total = operLogQueryMapper.count(u, m, s);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, operLogQueryMapper.search(u, m, s, pq.getSize(), pq.getOffset()));
    }

    /**
     * 删除一条操作日志。
     *
     * @param id 日志主键
     */
    @Transactional
    public void remove(Long id) {
        int n = operLogQueryMapper.deleteById(id);
        if (n == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "操作日志不存在");
        }
    }

    /**
     * 空白字符串转 {@code null}。
     *
     * @param value 原始字符串
     * @return 非空白时返回 trim 后的值，否则 {@code null}
     */
    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
