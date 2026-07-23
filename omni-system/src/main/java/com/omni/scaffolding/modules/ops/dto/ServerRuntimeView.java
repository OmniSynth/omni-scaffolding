package com.omni.scaffolding.modules.ops.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 当前运行环境全量快照。
 */
@Data
public class ServerRuntimeView {

    /**
     * 应用信息。
     */
    private AppInfo app = new AppInfo();

    /**
     * JVM 信息。
     */
    private JvmInfo jvm = new JvmInfo();

    /**
     * 内存信息。
     */
    private MemoryInfo memory = new MemoryInfo();

    /**
     * 操作系统信息。
     */
    private OsInfo os = new OsInfo();

    /**
     * 磁盘列表。
     */
    private List<DiskInfo> disks = new ArrayList<>();

    /**
     * 数据源连接池信息。
     */
    private DataSourceInfo dataSource = new DataSourceInfo();

    /**
     * Redis 运行时信息。
     */
    private RedisRuntimeInfo redis = new RedisRuntimeInfo();

    /**
     * 系统属性（敏感项已脱敏）。
     */
    private Map<String, String> systemProperties = new LinkedHashMap<>();

    /**
     * 环境变量（敏感项已脱敏）。
     */
    private Map<String, String> environment = new LinkedHashMap<>();

    /**
     * 采集时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Instant collectedAt;

    /**
     * 应用基本信息。
     */
    @Data
    public static class AppInfo {
        /**
         * 应用名称。
         */
        private String name;
        /**
         * 应用版本。
         */
        private String version;
        /**
         * 激活的 Spring Profile。
         */
        private List<String> activeProfiles = new ArrayList<>();
        /**
         * 进程启动时间。
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
        private Instant startTime;
        /**
         * 已运行时长（毫秒）。
         */
        private Long uptimeMs;
        /**
         * JAVA_HOME。
         */
        private String javaHome;
        /**
         * 工作目录。
         */
        private String userDir;
        /**
         * 用户时区。
         */
        private String userTimezone;
        /**
         * 文件编码。
         */
        private String fileEncoding;
        /**
         * 是否启用虚拟线程。
         */
        private Boolean virtualThreadsEnabled;
    }

    /**
     * JVM 运行时信息。
     */
    @Data
    public static class JvmInfo {
        /**
         * 虚拟机名称。
         */
        private String name;
        /**
         * Java 版本。
         */
        private String version;
        /**
         * 厂商。
         */
        private String vendor;
        /**
         * 运行时名称。
         */
        private String runtimeName;
        /**
         * VM 名称。
         */
        private String vmName;
        /**
         * 进程 ID。
         */
        private String pid;
        /**
         * JVM 启动参数。
         */
        private List<String> inputArguments = new ArrayList<>();
        /**
         * 可用处理器数。
         */
        private Integer availableProcessors;
        /**
         * 当前线程数。
         */
        private Long threadCount;
        /**
         * 峰值线程数。
         */
        private Long peakThreadCount;
        /**
         * 守护线程数。
         */
        private Long daemonThreadCount;
        /**
         * 已加载类数。
         */
        private Long loadedClassCount;
        /**
         * 已卸载类数。
         */
        private Long unloadedClassCount;
        /**
         * 进程 CPU 使用率（%）。
         */
        private Double processCpuLoad;
        /**
         * 系统 CPU 使用率（%）。
         */
        private Double systemCpuLoad;
    }

    /**
     * 内存使用情况。
     */
    @Data
    public static class MemoryInfo {
        /**
         * 堆初始大小（字节）。
         */
        private Long heapInit;
        /**
         * 堆已用（字节）。
         */
        private Long heapUsed;
        /**
         * 堆已提交（字节）。
         */
        private Long heapCommitted;
        /**
         * 堆最大值（字节）。
         */
        private Long heapMax;
        /**
         * 非堆初始大小（字节）。
         */
        private Long nonHeapInit;
        /**
         * 非堆已用（字节）。
         */
        private Long nonHeapUsed;
        /**
         * 非堆已提交（字节）。
         */
        private Long nonHeapCommitted;
        /**
         * 非堆最大值（字节）。
         */
        private Long nonHeapMax;
        /**
         * Runtime 空闲内存（字节）。
         */
        private Long freeMemory;
        /**
         * Runtime 总内存（字节）。
         */
        private Long totalMemory;
        /**
         * Runtime 最大内存（字节）。
         */
        private Long maxMemory;
    }

    /**
     * 操作系统信息。
     */
    @Data
    public static class OsInfo {
        /**
         * 系统名称。
         */
        private String name;
        /**
         * 架构。
         */
        private String arch;
        /**
         * 版本。
         */
        private String version;
        /**
         * 可用处理器数。
         */
        private Integer availableProcessors;
        /**
         * 系统负载均值。
         */
        private Double systemLoadAverage;
        /**
         * 物理内存总量（字节）。
         */
        private Long totalMemorySize;
        /**
         * 物理内存空闲（字节）。
         */
        private Long freeMemorySize;
        /**
         * 主机名。
         */
        private String hostName;
    }

    /**
     * 磁盘分区信息。
     */
    @Data
    public static class DiskInfo {
        /**
         * 挂载路径。
         */
        private String path;
        /**
         * 总空间（字节）。
         */
        private Long totalSpace;
        /**
         * 空闲空间（字节）。
         */
        private Long freeSpace;
        /**
         * 可用空间（字节）。
         */
        private Long usableSpace;
    }

    /**
     * 数据源连接池信息。
     */
    @Data
    public static class DataSourceInfo {
        /**
         * 是否可用。
         */
        private Boolean available;
        /**
         * 连接池类型。
         */
        private String poolType;
        /**
         * JDBC URL（敏感项已脱敏）。
         */
        private String jdbcUrl;
        /**
         * 用户名。
         */
        private String username;
        /**
         * 驱动类名。
         */
        private String driverClassName;
        /**
         * 最大连接数。
         */
        private Integer maximumPoolSize;
        /**
         * 最小空闲连接数。
         */
        private Integer minimumIdle;
        /**
         * 活跃连接数。
         */
        private Integer activeConnections;
        /**
         * 空闲连接数。
         */
        private Integer idleConnections;
        /**
         * 总连接数。
         */
        private Integer totalConnections;
        /**
         * 等待连接的线程数。
         */
        private Integer threadsAwaitingConnection;
        /**
         * 附加说明或错误信息。
         */
        private String message;
    }

    /**
     * Redis 运行时信息。
     */
    @Data
    public static class RedisRuntimeInfo {
        /**
         * 是否可用。
         */
        private Boolean available;
        /**
         * PING 响应。
         */
        private String pong;
        /**
         * Redis 版本。
         */
        private String redisVersion;
        /**
         * 当前库 Key 数。
         */
        private Long dbSize;
        /**
         * 已用内存（人类可读）。
         */
        private String usedMemoryHuman;
        /**
         * 附加说明或错误信息。
         */
        private String message;
    }
}
