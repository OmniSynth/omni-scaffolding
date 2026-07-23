package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.api.PageQuery;
import com.omni.scaffolding.common.api.PageResult;
import com.omni.scaffolding.common.exception.BusinessException;
import com.omni.scaffolding.common.notify.NotifyEvents;
import com.omni.scaffolding.common.notify.NotifyMessage;
import com.omni.scaffolding.common.util.IdGenerator;
import com.omni.scaffolding.infra.notify.NotifyDispatcher;
import com.omni.scaffolding.modules.system.dto.notice.NoticeSaveRequest;
import com.omni.scaffolding.modules.system.dto.notice.NoticeView;
import com.omni.scaffolding.modules.system.entity.SysNotice;
import com.omni.scaffolding.modules.system.entity.SysNoticeRead;
import com.omni.scaffolding.modules.system.mapper.SysNoticeQueryMapper;
import com.omni.scaffolding.modules.system.repository.SysNoticeReadRepository;
import com.omni.scaffolding.modules.system.repository.SysNoticeRepository;
import com.omni.scaffolding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

/**
 * 通知公告：管理端维护，用户端未读提示与已读标记。
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final long INBOX_LIMIT = 50L;

    private final SysNoticeRepository noticeRepository;
    private final SysNoticeReadRepository noticeReadRepository;
    private final SysNoticeQueryMapper noticeQueryMapper;
    private final NotifyDispatcher notifyDispatcher;

    /**
     * 分页查询公告（管理端）。
     *
     * @param keyword 可选，匹配标题 / 内容
     * @param status  可选，启停状态
     * @param type    可选，公告类型
     * @param page    页码，从 1 开始
     * @param size    每页条数
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<NoticeView> list(String keyword, Boolean status, String type, Long page, Long size) {
        PageQuery pq = PageQuery.of(page, size);
        long total = noticeQueryMapper.countNotices(keyword, status, blankToNull(type));
        if (total == 0) {
            return pq.toResult(0, List.of());
        }
        return pq.toResult(total, noticeQueryMapper.listNotices(keyword, status, blankToNull(type), pq.getSize(), pq.getOffset()));
    }

    /**
     * 公告详情；不存在则 404。
     *
     * @param noticeId 公告主键
     * @return 读模型
     */
    @Transactional(readOnly = true)
    public NoticeView detail(Long noticeId) {
        NoticeView view = noticeQueryMapper.findById(noticeId);
        if (view == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公告不存在");
        }
        return view;
    }

    /**
     * 新增公告；首次启用时写入发布人与发布时间。
     *
     * @param request 创建请求
     * @return 新建公告读模型
     */
    @Transactional
    public NoticeView create(NoticeSaveRequest request) {
        SysNotice notice = new SysNotice();
        notice.setId(IdGenerator.nextId());
        notice.setDeleted(0);
        applyMutable(notice, request);
        fillPublishMeta(notice, Boolean.TRUE.equals(request.getStatus()), true);
        // 需先刷盘，否则同事务内 MyBatis 读不到未 flush 的 JPA 写入
        noticeRepository.saveAndFlush(notice);
        if (Boolean.TRUE.equals(notice.getStatus())) {
            dispatchPublished(notice);
        }
        return detail(notice.getId());
    }

    /**
     * 修改公告；从停用变为启用时补写发布元数据。
     *
     * @param noticeId 公告主键
     * @param request  修改请求
     * @return 更新后的读模型
     */
    @Transactional
    public NoticeView update(Long noticeId, NoticeSaveRequest request) {
        SysNotice notice = requireNotice(noticeId);
        boolean wasEnabled = Boolean.TRUE.equals(notice.getStatus());
        applyMutable(notice, request);
        boolean nowEnabled = Boolean.TRUE.equals(notice.getStatus());
        boolean firstEnable = !wasEnabled && nowEnabled;
        fillPublishMeta(notice, nowEnabled, firstEnable);
        noticeRepository.saveAndFlush(notice);
        if (firstEnable) {
            dispatchPublished(notice);
        }
        return detail(noticeId);
    }

    /**
     * 切换公告启停；首次启用时写入发布人与发布时间。
     *
     * @param noticeId 公告主键
     * @param status   是否启用
     * @return 更新后的读模型
     */
    @Transactional
    public NoticeView changeStatus(Long noticeId, boolean status) {
        SysNotice notice = requireNotice(noticeId);
        boolean wasEnabled = Boolean.TRUE.equals(notice.getStatus());
        notice.setStatus(status);
        boolean firstEnable = !wasEnabled && status;
        fillPublishMeta(notice, status, firstEnable);
        noticeRepository.saveAndFlush(notice);
        if (firstEnable) {
            dispatchPublished(notice);
        }
        return detail(noticeId);
    }

    /**
     * 逻辑删除公告。
     *
     * @param noticeId 公告主键
     */
    @Transactional
    public void remove(Long noticeId) {
        SysNotice notice = requireNotice(noticeId);
        notice.setDeleted(1);
        noticeRepository.save(notice);
    }

    /**
     * 列出当前登录用户未读的启用公告。
     *
     * @return 读模型列表，可能为空
     */
    @Transactional(readOnly = true)
    public List<NoticeView> listUnread() {
        return noticeQueryMapper.listUnread(SecurityUtils.requireUserId());
    }

    /**
     * 统计当前登录用户未读公告数。
     *
     * @return 未读数量
     */
    @Transactional(readOnly = true)
    public long unreadCount() {
        return noticeQueryMapper.countUnread(SecurityUtils.requireUserId());
    }

    /**
     * 查询当前登录用户公告收件箱（含已读标记，最多 50 条）。
     *
     * @return 读模型列表，可能为空
     */
    @Transactional(readOnly = true)
    public List<NoticeView> inbox() {
        return noticeQueryMapper.listInbox(SecurityUtils.requireUserId(), INBOX_LIMIT);
    }

    /**
     * 标记单条公告为已读（幂等）。
     *
     * @param noticeId 公告主键
     */
    @Transactional
    public void markRead(Long noticeId) {
        Long userId = SecurityUtils.requireUserId();
        NoticeView notice = noticeQueryMapper.findById(noticeId);
        if (notice == null || !Boolean.TRUE.equals(notice.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公告不存在或已停用");
        }
        if (noticeReadRepository.existsByNoticeIdAndUserId(noticeId, userId)) {
            return;
        }
        SysNoticeRead read = new SysNoticeRead();
        read.setId(IdGenerator.nextId());
        read.setNoticeId(noticeId);
        read.setUserId(userId);
        read.setReadTime(Instant.now());
        noticeReadRepository.save(read);
    }

    /**
     * 标记当前用户全部未读公告为已读。
     */
    @Transactional
    public void markAllRead() {
        Long userId = SecurityUtils.requireUserId();
        List<Long> unreadIds = noticeQueryMapper.listUnreadIds(userId);
        Instant now = Instant.now();
        for (Long noticeId : unreadIds) {
            if (noticeReadRepository.existsByNoticeIdAndUserId(noticeId, userId)) {
                continue;
            }
            SysNoticeRead read = new SysNoticeRead();
            read.setId(IdGenerator.nextId());
            read.setNoticeId(noticeId);
            read.setUserId(userId);
            read.setReadTime(now);
            noticeReadRepository.save(read);
        }
    }

    /**
     * 按主键加载公告；不存在则 404。
     *
     * @param noticeId 公告主键
     * @return 未删除的公告实体
     */
    private SysNotice requireNotice(Long noticeId) {
        return noticeRepository.findByIdAndDeleted(noticeId, 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "公告不存在"));
    }

    /**
     * 将请求中的可变字段写入公告实体。
     *
     * @param notice  目标实体
     * @param request 保存请求
     */
    private void applyMutable(SysNotice notice, NoticeSaveRequest request) {
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        notice.setType(request.getType().trim());
        notice.setStatus(Boolean.TRUE.equals(request.getStatus()));
    }

    /**
     * 首次启用时写入发布人与发布时间；已发布过则保留。
     */
    private void fillPublishMeta(SysNotice notice, boolean enabled, boolean firstEnable) {
        if (!enabled) {
            return;
        }
        if (firstEnable || notice.getPublishTime() == null) {
            notice.setPublisherId(SecurityUtils.requireUserId());
            notice.setPublishTime(Instant.now());
        }
    }

    /**
     * 公告首次发布时走通知 SPI（默认日志通道；可扩展邮件/短信）。
     */
    private void dispatchPublished(SysNotice notice) {
        notifyDispatcher.dispatch(NotifyMessage.of(
                NotifyEvents.NOTICE_PUBLISHED,
                notice.getTitle(),
                notice.getContent(),
                notice.getType(),
                notice.getId()));
    }

    /**
     * 空白字符串转 {@code null}。
     *
     * @param value 原始字符串
     * @return 非空白时返回 trim 后的值，否则 {@code null}
     */
    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
