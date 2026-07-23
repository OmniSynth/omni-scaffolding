package com.omni.scaffolding.common.file;

/**
 * 存储类型常量及配置值归一化。
 *
 * <p>库表 {@code sys_file.storage_type} 存大写常量；YAML {@code omni.file.storage-type} 用小写配置项。
 */
public final class StorageTypes {

    /**
     * 本地磁盘。
     */
    public static final String LOCAL = "LOCAL";

    /**
     * MinIO（S3 兼容）。
     */
    public static final String MINIO = "MINIO";

    /**
     * 对象存储（经 {@link OssProviderPlugin} 路由到具体厂商）。
     */
    public static final String OSS = "OSS";

    private StorageTypes() {
    }

    /**
     * 将库表/运行时字符串规范为大写类型；空值视为 {@link #LOCAL}。
     *
     * @param raw 原始类型字符串
     * @return {@link #LOCAL} / {@link #MINIO} / {@link #OSS} 或原串大写形式
     */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return LOCAL;
        }
        return raw.trim().toUpperCase();
    }

    /**
     * 将配置项 {@code omni.file.storage-type}（local/minio/oss）转为库表常量。
     *
     * @param storageType 配置值，可空
     * @return {@link #LOCAL} / {@link #MINIO} / {@link #OSS}
     */
    public static String fromConfig(String storageType) {
        if (storageType == null || storageType.isBlank()) {
            return LOCAL;
        }
        return switch (storageType.trim().toLowerCase()) {
            case "minio" -> MINIO;
            case "oss" -> OSS;
            default -> LOCAL;
        };
    }
}
