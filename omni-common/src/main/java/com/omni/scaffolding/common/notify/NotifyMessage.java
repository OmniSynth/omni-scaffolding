package com.omni.scaffolding.common.notify;

import lombok.Data;

/**
 * 跨通道通知消息（纯 DTO，无 Spring 依赖）。
 */
@Data
public class NotifyMessage {

    /**
     * 事件名，如 {@link NotifyEvents#NOTICE_PUBLISHED}。
     */
    private String event;

    /**
     * 标题。
     */
    private String title;

    /**
     * 正文。
     */
    private String content;

    /**
     * 业务类型，如公告 type：ANNOUNCE / NOTICE。
     */
    private String bizType;

    /**
     * 业务主键（如公告 id）。
     */
    private Long bizId;

    /**
     * 可选扩展 JSON / 备注。
     */
    private String extra;

    public static NotifyMessage of(String event, String title, String content, String bizType, Long bizId) {
        NotifyMessage message = new NotifyMessage();
        message.setEvent(event);
        message.setTitle(title);
        message.setContent(content);
        message.setBizType(bizType);
        message.setBizId(bizId);
        return message;
    }
}
