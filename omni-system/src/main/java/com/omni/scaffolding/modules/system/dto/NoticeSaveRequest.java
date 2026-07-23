package com.omni.scaffolding.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 / 修改通知公告请求。
 */
@Data
public class NoticeSaveRequest {

    /**
     * 标题。
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    /**
     * 正文内容。
     */
    @NotBlank(message = "内容不能为空")
    @Size(max = 4000, message = "内容长度不能超过 4000")
    private String content;

    /**
     * 类型：NOTICE / ANNOUNCE。
     */
    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "NOTICE|ANNOUNCE", message = "类型只能为 NOTICE 或 ANNOUNCE")
    private String type = "NOTICE";

    /**
     * 是否启用。
     */
    @NotNull(message = "状态不能为空")
    private Boolean status = true;
}
