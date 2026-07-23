package com.omni.scaffolding.modules.system.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件短时预览 URL 响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilePreviewUrlView {

    /**
     * 文件主键。
     */
    private Long fileId;

    /**
     * 带 {@code expire}/{@code sign} 的 content 相对路径。
     */
    private String url;

    /**
     * 签名过期时间（Unix 纪元秒）。
     */
    private long expire;
}
