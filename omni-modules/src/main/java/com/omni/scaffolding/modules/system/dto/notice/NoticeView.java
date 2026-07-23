package com.omni.scaffolding.modules.system.dto.notice;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

/**
 * 通知公告读模型。
 */
@Data
public class NoticeView {

    /**
     * 主键。
     */
    private Long id;

    /**
     * 标题。
     */
    private String title;

    /**
     * 正文内容。
     */
    private String content;

    /**
     * 类型：NOTICE / ANNOUNCE。
     */
    private String type;

    /**
     * 是否启用。
     */
    private Boolean status;

    /**
     * 发布人用户主键。
     */
    private Long publisherId;

    /**
     * 发布人姓名。
     */
    private String publisherName;

    /**
     * 发布时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Instant publishTime;

    /**
     * 收件箱场景：当前用户是否已读。
     */
    private Boolean readFlag;
}
