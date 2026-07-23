package com.omni.scaffolding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地文件上传配置，前缀 {@code omni.upload}。
 */
@Data
@Component
@ConfigurationProperties(prefix = "omni.upload")
public class OmniUploadProperties {

    /**
     * 本地存储根目录（相对运行目录或绝对路径）。
     */
    private String baseDir = "./data/uploads";

    /**
     * 对外访问 URL 前缀，需与静态资源映射一致。
     */
    private String urlPrefix = "/uploads";

    /**
     * 头像最大字节数，默认 2MB。
     */
    private long avatarMaxBytes = 2 * 1024 * 1024L;
}
