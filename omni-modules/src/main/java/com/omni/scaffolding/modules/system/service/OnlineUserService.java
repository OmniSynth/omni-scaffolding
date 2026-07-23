package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.modules.system.dto.user.OnlineUserView;
import com.omni.scaffolding.modules.system.dto.user.UserDetailView;
import com.omni.scaffolding.modules.system.mapper.SysUserQueryMapper;
import com.omni.scaffolding.security.SecurityUtils;
import com.omni.scaffolding.security.online.OnlineSession;
import com.omni.scaffolding.security.online.OnlineSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * 在线用户查询与强制下线。
 *
 * <p>基于 Redis 在线会话列表；踢下线会将 jti 加入黑名单。
 */
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final OnlineSessionService onlineSessionService;
    private final SysUserQueryMapper userQueryMapper;

    /**
     * 列出在线会话；可按用户名 / IP 模糊过滤。
     *
     * @param username 可选，模糊匹配用户名
     * @param ip       可选，模糊匹配 IP
     * @return 在线会话列表
     */
    public List<OnlineUserView> list(String username, String ip) {
        String usernameKeyword = normalize(username);
        String ipKeyword = normalize(ip);
        return onlineSessionService.listOnline().stream()
                .filter(session -> match(session.getUsername(), usernameKeyword))
                .filter(session -> match(session.getIp(), ipKeyword))
                .map(this::toView)
                .toList();
    }

    /**
     * 按会话 jti 强制下线。
     *
     * @param jti 会话 jti
     */
    public void kick(String jti) {
        if (!StringUtils.hasText(jti)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话标识不能为空");
        }
        String currentJti = SecurityUtils.requireAuthUser().getJti();
        if (jti.equals(currentJti)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能踢出当前登录会话，请使用退出登录");
        }
        onlineSessionService.kickByJti(jti.trim());
    }

    /**
     * 在线会话转前端视图，并补充部门名称等资料。
     *
     * @param session Redis 中的在线会话
     * @return 在线用户视图
     */
    private OnlineUserView toView(OnlineSession session) {
        OnlineUserView view = new OnlineUserView();
        view.setJti(session.getJti());
        view.setUserId(session.getUserId());
        view.setUsername(session.getUsername());
        view.setDeptId(session.getDeptId());
        view.setIp(session.getIp());
        view.setUserAgent(session.getUserAgent());
        view.setLoginTime(session.getLoginTime());
        view.setExpireAt(session.getExpireAt());
        if (session.getUserId() != null) {
            UserDetailView detail = userQueryMapper.findUserDetail(session.getUserId());
            if (detail != null) {
                view.setDeptName(detail.getDeptName());
                if (!StringUtils.hasText(view.getUsername())) {
                    view.setUsername(detail.getUsername());
                }
            }
        }
        return view;
    }

    /**
     * 去首尾空白并转小写，用于不区分大小写的匹配。
     *
     * @param value 原始字符串
     * @return 规范化后的字符串，{@code null} 输入返回 {@code null}
     */
    private static String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 判断源字符串是否包含关键字（忽略大小写；关键字为空时视为匹配）。
     *
     * @param source  待匹配源
     * @param keyword 搜索关键字
     * @return 匹配返回 {@code true}
     */
    private static boolean match(String source, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        if (!StringUtils.hasText(source)) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
