package com.omni.scaffolding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一文件存储配置，前缀 {@code omni.file}。
 *
 * <p>{@code storage-type} 控制<strong>写入</strong>目标：{@code local} / {@code minio} / {@code oss}。
 * 选 {@code oss} 时再通过 {@code oss.provider} 选择具体 {@link com.omni.scaffolding.common.file.OssProviderPlugin}。
 * 每条 {@code sys_file} 记录实际存储类型，读历史文件不依赖当前激活配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "omni.file")
public class OmniFileProperties {

    /**
     * 写入存储类型：local / minio / oss。
     */
    private String storageType = "local";

    /**
     * 通用上传大小上限（字节），默认 20MB。
     */
    private long maxBytes = 20 * 1024 * 1024L;

    /**
     * 头像专用上限（字节），默认 2MB；{@code bizType=avatar} 时生效。
     */
    private long avatarMaxBytes = 2 * 1024 * 1024L;

    /**
     * 允许的 Content-Type 白名单；空表示不按 MIME 限制（扩展名仍可由业务校验）。
     */
    private List<String> allowedContentTypes = new ArrayList<>();

    /**
     * 文件内容短时签名密钥；空则回落到 JWT secret。
     */
    private String signSecret;

    /**
     * 预览签名有效期（秒），默认 900；建议不小于用户详情缓存 TTL。
     */
    private long signTtlSeconds = 900L;

    /**
     * 逻辑删除元数据时是否同步物理删除对象。
     */
    private boolean physicalDelete = true;

    /**
     * 本地磁盘配置。
     */
    private Local local = new Local();

    /**
     * MinIO 配置（{@code storage-type=minio} 时必填 endpoint/密钥）。
     */
    private Minio minio = new Minio();

    /**
     * OSS 插件与各厂商凭证。
     */
    private Oss oss = new Oss();

    /**
     * 本地存储子配置。
     */
    @Data
    public static class Local {
        /**
         * 本地存储根目录（相对运行目录或绝对路径）。
         */
        private String baseDir = "./data/uploads";
    }

    /**
     * MinIO 子配置。
     */
    @Data
    public static class Minio {
        /**
         * 服务地址，如 {@code http://127.0.0.1:9000}。
         */
        private String endpoint;
        /**
         * Access Key。
         */
        private String accessKey;
        /**
         * Secret Key。
         */
        private String secretKey;
        /**
         * 桶名，默认 {@code omni}；不存在时尝试自动创建。
         */
        private String bucket = "omni";
        /**
         * 是否使用 path-style（MinIO 常见为 true）。
         */
        private boolean pathStyle = true;
        /**
         * 对象键可选统一前缀。
         */
        private String dirPrefix = "";
    }

    /**
     * OSS 子配置。
     */
    @Data
    public static class Oss {
        /**
         * 当前激活的 OSS 插件 id，如 aliyun / qiniu。
         */
        private String provider = "aliyun";
        /**
         * 各厂商配置；key = providerId，value = 该厂商键值对（endpoint、密钥、bucket 等）。
         */
        private Map<String, Map<String, String>> providers = new LinkedHashMap<>();
    }

    /**
     * 解析有效签名密钥：优先 {@link #signSecret}，否则使用 JWT 回落密钥。
     *
     * @param jwtSecretFallback JWT secret 作为回落
     * @return 非空签名密钥
     */
    public String resolveSignSecret(String jwtSecretFallback) {
        if (signSecret != null && !signSecret.isBlank()) {
            return signSecret;
        }
        return jwtSecretFallback;
    }
}
