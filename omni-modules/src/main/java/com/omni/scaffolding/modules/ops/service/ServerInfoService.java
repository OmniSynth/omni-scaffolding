package com.omni.scaffolding.modules.ops.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.omni.scaffolding.common.persistence.DataSourceKeys;
import com.omni.scaffolding.infra.redis.RedisService;
import com.omni.scaffolding.modules.ops.dto.ServerRuntimeView;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * 采集当前进程运行环境全量信息（敏感项脱敏）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerInfoService {

    private static final Pattern SENSITIVE = Pattern.compile(
            "(?i).*(password|passwd|secret|token|credential|private.?key|access.?key|api.?key|jdbc\\.password).*");

    private final Environment environment;
    private final ObjectProvider<DataSource> dataSourceProvider;
    private final ObjectProvider<RedisService> redisServiceProvider;
    private final ObjectProvider<BuildProperties> buildPropertiesProvider;

    /**
     * 采集当前进程运行环境全量信息（敏感项脱敏）。
     *
     * @return 运行环境快照
     */
    public ServerRuntimeView collect() {
        ServerRuntimeView view = new ServerRuntimeView();
        view.setCollectedAt(Instant.now());
        fillApp(view.getApp());
        fillJvm(view.getJvm());
        fillMemory(view.getMemory());
        fillOs(view.getOs());
        fillDisks(view);
        fillDataSource(view.getDataSource());
        fillRedis(view.getRedis());
        view.setSystemProperties(filterMap(System.getProperties()));
        view.setEnvironment(filterEnv(System.getenv()));
        return view;
    }

    /**
     * 填充应用运行时信息（名称、版本、启动时间、环境等）。
     *
     * @param app 应用信息容器
     */
    private void fillApp(ServerRuntimeView.AppInfo app) {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        app.setName(environment.getProperty("spring.application.name", "omni-scaffolding"));
        BuildProperties build = buildPropertiesProvider.getIfAvailable();
        app.setVersion(build != null ? build.getVersion() : environment.getProperty("omni.version", "1.0.0-SNAPSHOT"));
        app.setActiveProfiles(Arrays.asList(environment.getActiveProfiles()));
        Instant start = Instant.ofEpochMilli(runtime.getStartTime());
        app.setStartTime(start);
        app.setUptimeMs(Duration.between(start, Instant.now()).toMillis());
        app.setJavaHome(System.getProperty("java.home"));
        app.setUserDir(System.getProperty("user.dir"));
        app.setUserTimezone(System.getProperty("user.timezone"));
        app.setFileEncoding(System.getProperty("file.encoding"));
        app.setVirtualThreadsEnabled(
                environment.getProperty("spring.threads.virtual.enabled", Boolean.class, Boolean.FALSE));
    }

    /**
     * 填充 JVM 运行时指标（版本、线程、类加载、CPU 负载等）。
     *
     * @param jvm JVM 信息容器
     */
    private void fillJvm(ServerRuntimeView.JvmInfo jvm) {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        ClassLoadingMXBean classes = ManagementFactory.getClassLoadingMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

        jvm.setName(System.getProperty("java.vm.name"));
        jvm.setVersion(System.getProperty("java.version"));
        jvm.setVendor(System.getProperty("java.vendor"));
        jvm.setRuntimeName(runtime.getName());
        jvm.setVmName(runtime.getVmName());
        String name = runtime.getName();
        if (name != null && name.contains("@")) {
            jvm.setPid(name.substring(0, name.indexOf('@')));
        } else {
            jvm.setPid(name);
        }
        jvm.setInputArguments(runtime.getInputArguments());
        jvm.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
        jvm.setThreadCount((long) threads.getThreadCount());
        jvm.setPeakThreadCount((long) threads.getPeakThreadCount());
        jvm.setDaemonThreadCount((long) threads.getDaemonThreadCount());
        jvm.setLoadedClassCount((long) classes.getLoadedClassCount());
        jvm.setUnloadedClassCount(classes.getUnloadedClassCount());

        if (os instanceof com.sun.management.OperatingSystemMXBean sunOs) {
            jvm.setProcessCpuLoad(roundLoad(sunOs.getProcessCpuLoad()));
            jvm.setSystemCpuLoad(roundLoad(sunOs.getCpuLoad()));
        }
    }

    /**
     * 填充堆 / 非堆内存用量。
     *
     * @param memory 内存信息容器
     */
    private void fillMemory(ServerRuntimeView.MemoryInfo memory) {
        MemoryMXBean mx = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = mx.getHeapMemoryUsage();
        MemoryUsage nonHeap = mx.getNonHeapMemoryUsage();
        Runtime rt = Runtime.getRuntime();

        memory.setHeapInit(heap.getInit());
        memory.setHeapUsed(heap.getUsed());
        memory.setHeapCommitted(heap.getCommitted());
        memory.setHeapMax(heap.getMax());
        memory.setNonHeapInit(nonHeap.getInit());
        memory.setNonHeapUsed(nonHeap.getUsed());
        memory.setNonHeapCommitted(nonHeap.getCommitted());
        memory.setNonHeapMax(nonHeap.getMax());
        memory.setFreeMemory(rt.freeMemory());
        memory.setTotalMemory(rt.totalMemory());
        memory.setMaxMemory(rt.maxMemory());
    }

    /**
     * 填充操作系统信息（名称、架构、负载、主机名等）。
     *
     * @param osInfo 操作系统信息容器
     */
    private void fillOs(ServerRuntimeView.OsInfo osInfo) {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        osInfo.setName(os.getName());
        osInfo.setArch(os.getArch());
        osInfo.setVersion(os.getVersion());
        osInfo.setAvailableProcessors(os.getAvailableProcessors());
        osInfo.setSystemLoadAverage(os.getSystemLoadAverage());
        try {
            osInfo.setHostName(InetAddress.getLocalHost().getHostName());
        } catch (Exception ex) {
            osInfo.setHostName("unknown");
        }
        if (os instanceof com.sun.management.OperatingSystemMXBean sunOs) {
            osInfo.setTotalMemorySize(sunOs.getTotalMemorySize());
            osInfo.setFreeMemorySize(sunOs.getFreeMemorySize());
        }
    }

    /**
     * 扫描文件系统根路径并填充磁盘空间信息。
     *
     * @param view 运行时视图（写入 {@code disks} 列表）
     */
    private void fillDisks(ServerRuntimeView view) {
        File[] roots = File.listRoots();
        if (roots == null) {
            return;
        }
        for (File root : roots) {
            ServerRuntimeView.DiskInfo disk = new ServerRuntimeView.DiskInfo();
            disk.setPath(root.getAbsolutePath());
            disk.setTotalSpace(root.getTotalSpace());
            disk.setFreeSpace(root.getFreeSpace());
            disk.setUsableSpace(root.getUsableSpace());
            view.getDisks().add(disk);
        }
    }

    /**
     * 探测并填充数据源连接池状态（动态主从 / Druid / HikariCP 等）。
     *
     * @param info 数据源信息容器
     */
    private void fillDataSource(ServerRuntimeView.DataSourceInfo info) {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            info.setAvailable(false);
            info.setMessage("未装配 DataSource");
            return;
        }
        info.setAvailable(true);
        if (dataSource instanceof DynamicRoutingDataSource dynamic) {
            Map<String, DataSource> dataSources = dynamic.getDataSources();
            DataSource master = dataSources.get(DataSourceKeys.MASTER);
            if (master == null && !dataSources.isEmpty()) {
                master = dataSources.values().iterator().next();
            }
            if (master == null) {
                info.setAvailable(false);
                info.setMessage("动态数据源未注册任何节点");
                return;
            }
            fillPoolMetrics(master, info);
            String keys = String.join(",", dataSources.keySet());
            String prefix = info.getPoolType() == null ? "Dynamic" : info.getPoolType() + "/Dynamic";
            info.setPoolType(prefix);
            String existing = info.getMessage();
            info.setMessage((existing == null || existing.isBlank() ? "" : existing + "; ")
                    + "dynamic keys=[" + keys + "], metrics=master");
            return;
        }
        fillPoolMetrics(dataSource, info);
    }

    /**
     * 填充具体连接池指标。
     */
    private void fillPoolMetrics(DataSource dataSource, ServerRuntimeView.DataSourceInfo info) {
        if (dataSource instanceof DruidDataSource druid) {
            info.setPoolType("Druid");
            info.setJdbcUrl(maskSensitiveValue(druid.getUrl()));
            info.setUsername(druid.getUsername());
            info.setDriverClassName(druid.getDriverClassName());
            info.setMaximumPoolSize(druid.getMaxActive());
            info.setMinimumIdle(druid.getMinIdle());
            info.setActiveConnections(druid.getActiveCount());
            info.setIdleConnections(druid.getPoolingCount());
            info.setTotalConnections(druid.getPoolingCount() + druid.getActiveCount());
            info.setThreadsAwaitingConnection(druid.getWaitThreadCount());
        } else if (dataSource instanceof HikariDataSource hikari) {
            info.setPoolType("HikariCP");
            info.setJdbcUrl(maskSensitiveValue(hikari.getJdbcUrl()));
            info.setUsername(hikari.getUsername());
            info.setDriverClassName(hikari.getDriverClassName());
            info.setMaximumPoolSize(hikari.getMaximumPoolSize());
            info.setMinimumIdle(hikari.getMinimumIdle());
            try {
                HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
                if (pool != null) {
                    info.setActiveConnections(pool.getActiveConnections());
                    info.setIdleConnections(pool.getIdleConnections());
                    info.setTotalConnections(pool.getTotalConnections());
                    info.setThreadsAwaitingConnection(pool.getThreadsAwaitingConnection());
                }
            } catch (Exception ex) {
                info.setMessage("连接池指标暂不可用: " + ex.getMessage());
            }
        } else {
            info.setPoolType(dataSource.getClass().getSimpleName());
            info.setJdbcUrl(maskSensitiveValue(firstNonBlank(
                    environment.getProperty("spring.datasource.dynamic.datasource.master.url"),
                    environment.getProperty("spring.datasource.url"))));
            info.setUsername(firstNonBlank(
                    environment.getProperty("spring.datasource.dynamic.datasource.master.username"),
                    environment.getProperty("spring.datasource.username")));
            info.setDriverClassName(firstNonBlank(
                    environment.getProperty("spring.datasource.dynamic.datasource.master.driver-class-name"),
                    environment.getProperty("spring.datasource.driver-class-name")));
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return second;
    }

    /**
     * 探测 Redis 连通性并填充版本、内存等运行时信息。
     *
     * @param info Redis 信息容器
     */
    private void fillRedis(ServerRuntimeView.RedisRuntimeInfo info) {
        RedisService redisService = redisServiceProvider.getIfAvailable();
        if (redisService == null) {
            info.setAvailable(false);
            info.setMessage("未装配 RedisService（测试环境或未启用 Redis）");
            return;
        }
        StringRedisTemplate template = redisService.template();
        try {
            String pong = template.execute((RedisConnection connection) -> {
                String reply = connection.ping();
                return reply == null ? "PONG" : reply;
            });
            info.setAvailable(true);
            info.setPong(pong);
            Properties props = template.execute((RedisConnection connection) -> connection.serverCommands().info("server"));
            Long dbSize = template.execute(RedisConnection::dbSize);
            info.setDbSize(dbSize);
            if (props != null) {
                Object version = props.get("redis_version");
                if (version != null) {
                    info.setRedisVersion(String.valueOf(version));
                }
            }
            Properties memory = template.execute((RedisConnection connection) -> connection.serverCommands().info("memory"));
            if (memory != null && memory.get("used_memory_human") != null) {
                info.setUsedMemoryHuman(String.valueOf(memory.get("used_memory_human")));
            }
        } catch (Exception ex) {
            info.setAvailable(false);
            info.setMessage("Redis 不可用: " + ex.getMessage());
            log.debug("collect redis runtime failed", ex);
        }
    }

    /**
     * 过滤系统属性并对敏感键脱敏。
     *
     * @param properties JVM 系统属性
     * @return 按 key 排序的键值映射
     */
    private static Map<String, String> filterMap(Properties properties) {
        Map<String, String> result = new TreeMap<>();
        for (String name : properties.stringPropertyNames()) {
            String value = properties.getProperty(name);
            result.put(name, isSensitive(name) ? maskSensitiveValue(value) : value);
        }
        return result;
    }

    /**
     * 过滤环境变量并对敏感键脱敏。
     *
     * @param env 系统环境变量
     * @return 按 key 排序的键值映射
     */
    private static Map<String, String> filterEnv(Map<String, String> env) {
        Map<String, String> result = new TreeMap<>();
        if (env == null) {
            return result;
        }
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = entry.getKey();
            result.put(key, isSensitive(key) ? maskSensitiveValue(entry.getValue()) : entry.getValue());
        }
        return result;
    }

    /**
     * 判断配置键名是否命中敏感词模式。
     *
     * @param key 配置键名
     * @return 敏感键返回 {@code true}
     */
    private static boolean isSensitive(String key) {
        return StringUtils.hasText(key) && SENSITIVE.matcher(key).matches();
    }

    /**
     * 对敏感配置值做部分掩码（保留首尾各 2 字符）。
     *
     * @param value 原始值
     * @return 掩码后的值
     */
    private static String maskSensitiveValue(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    /**
     * 将 CPU 负载转为保留两位小数的百分比；无效值返回 {@code null}。
     *
     * @param load 原始负载（0~1）
     * @return 百分比，无效时为 {@code null}
     */
    private static Double roundLoad(double load) {
        if (load < 0) {
            return null;
        }
        return Math.round(load * 10_000d) / 100d;
    }
}
