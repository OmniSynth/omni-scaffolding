package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.trace.TraceContext;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.modules.system.dto.log.LoginLogView;
import com.omni.scaffolding.modules.system.entity.SysLoginLog;
import com.omni.scaffolding.modules.system.mapper.SysLoginLogQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysLoginLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 登录日志服务。
 *
 * <p>写入使用独立事务，避免登录失败回滚时日志一并丢失。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private static final int MAX_UA = 512;

    private final SysLoginLogRepository loginLogRepository;
    private final SysLoginLogQueryMapper loginLogQueryMapper;

    /**
     * 记录一次登录结果（独立事务）。
     *
     * @param userId    用户主键，失败时可为 {@code null}
     * @param username  登录名
     * @param ip        客户端 IP
     * @param userAgent User-Agent
     * @param success   是否成功
     * @param message   结果说明
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String username, String ip, String userAgent, boolean success, String message) {
        try {
            SysLoginLog row = new SysLoginLog();
            row.setId(IdGenerator.nextId());
            row.setUserId(userId);
            row.setUsername(username == null ? "" : username.trim());
            row.setIp(ip);
            row.setUserAgent(truncate(userAgent, MAX_UA));
            row.setStatus(success ? "SUCCESS" : "FAIL");
            row.setMessage(truncate(message, 255));
            row.setTraceId(TraceContext.getTraceId());
            row.setLoginTime(Instant.now());
            loginLogRepository.save(row);
        } catch (Exception ex) {
            log.warn("failed to persist login log: {}", ex.getMessage());
        }
    }

    /**
     * 条件分页查询登录日志。
     *
     * @param username 可选，匹配用户名
     * @param status   可选，SUCCESS / FAIL
     * @param ip       可选，匹配 IP
     * @param page     页码
     * @param size     每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<LoginLogView> search(String username, String status, String ip, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        String u = blankToNull(username);
        String s = blankToNull(status);
        String i = blankToNull(ip);
        long total = loginLogQueryMapper.count(u, s, i);
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, loginLogQueryMapper.search(u, s, i, pq.getSize(), pq.getOffset()));
    }

    /**
     * 删除一条登录日志。
     *
     * @param id 日志主键
     */
    @Transactional
    public void remove(Long id) {
        int n = loginLogQueryMapper.deleteById(id);
        if (n == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "登录日志不存在");
        }
    }

    /**
     * 截断字符串至指定最大长度。
     *
     * @param value 原始字符串
     * @param max   最大长度
     * @return 截断后的字符串，{@code null} 输入返回 {@code null}
     */
    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.length() > max ? v.substring(0, max) : v;
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
