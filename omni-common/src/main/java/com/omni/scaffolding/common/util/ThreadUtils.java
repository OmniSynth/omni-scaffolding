package com.omni.scaffolding.common.util;

import com.omni.scaffolding.common.thread.NamedThreadFactory;
import com.omni.scaffolding.common.thread.ThreadPoolBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * 线程与线程池工具入口。
 *
 * <h2>能力</h2>
 * <ul>
 *   <li>虚拟线程执行器（I/O 密集）</li>
 *   <li>平台线程池 / CPU 池 / 调度池</li>
 *   <li>优雅关闭、并行执行、带超时执行</li>
 * </ul>
 *
 * <h2>示例</h2>
 * <pre>{@code
 * try (ExecutorService vt = ThreadUtils.newVirtualExecutor("http-client")) {
 *     List<String> bodies = ThreadUtils.invokeAll(vt, tasks, Duration.ofSeconds(10));
 * }
 *
 * ThreadPoolExecutor cpu = ThreadUtils.newCpuPool("hash");
 * try {
 *     cpu.submit(() -> digest(data));
 * } finally {
 *     ThreadUtils.shutdownGracefully(cpu, Duration.ofSeconds(30));
 * }
 * }</pre>
 */
public final class ThreadUtils {

    private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);

    private ThreadUtils() {
    }

    // -------------------------------------------------------------------------
    // 创建
    // -------------------------------------------------------------------------

    /**
     * 每任务一条虚拟线程的执行器（无限并发，适合阻塞 I/O）。
     *
     * @param namePrefix 线程名前缀
     * @return 可关闭的 {@link ExecutorService}（建议 try-with-resources）
     */
    public static ExecutorService newVirtualExecutor(String namePrefix) {
        return Executors.newThreadPerTaskExecutor(new NamedThreadFactory(namePrefix, true, false));
    }

    /**
     * 固定大小平台线程池（有界队列 + CallerRuns 背压）。
     *
     * @param namePrefix 线程名前缀
     * @param nThreads   核心=最大线程数
     * @return 线程池
     */
    public static ThreadPoolExecutor newFixedPool(String namePrefix, int nThreads) {
        return ThreadPoolBuilder.create(namePrefix)
                .corePoolSize(nThreads)
                .maxPoolSize(nThreads)
                .queueCapacity(256)
                .callerRunsWhenRejected()
                .build();
    }

    /**
     * CPU 密集默认池：核心=CPU 核数，最大=2×核数，有界队列。
     *
     * @param namePrefix 线程名前缀
     * @return 线程池
     */
    public static ThreadPoolExecutor newCpuPool(String namePrefix) {
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        return ThreadPoolBuilder.create(namePrefix)
                .corePoolSize(cores)
                .maxPoolSize(cores * 2)
                .queueCapacity(500)
                .keepAlive(Duration.ofSeconds(60))
                .callerRunsWhenRejected()
                .build();
    }

    /**
     * I/O 密集平台线程池（仅在不能用虚拟线程时使用）。
     *
     * <p>核心偏大、队列较短，偏向创建线程而非堆积。
     *
     * @param namePrefix 线程名前缀
     * @return 线程池
     */
    public static ThreadPoolExecutor newIoPool(String namePrefix) {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        return ThreadPoolBuilder.create(namePrefix)
                .corePoolSize(cores * 2)
                .maxPoolSize(cores * 4)
                .queueCapacity(100)
                .keepAlive(Duration.ofSeconds(30))
                .allowCoreThreadTimeOut(true)
                .callerRunsWhenRejected()
                .build();
    }

    /**
     * 单线程池（保证任务串行）。
     *
     * @param namePrefix 线程名前缀
     * @return 线程池
     */
    public static ThreadPoolExecutor newSinglePool(String namePrefix) {
        return newFixedPool(namePrefix, 1);
    }

    /**
     * 定时 / 延迟任务调度池（平台线程）。
     *
     * @param namePrefix   线程名前缀
     * @param corePoolSize 核心线程数
     * @return 调度执行器
     */
    public static ScheduledExecutorService newScheduledPool(String namePrefix, int corePoolSize) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                requirePositive(corePoolSize, "corePoolSize"),
                new NamedThreadFactory(namePrefix, false, false)
        );
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        return executor;
    }

    /**
     * 返回平台线程池构建器，便于完全自定义参数。
     *
     * @param namePrefix 线程名前缀
     * @return 构建器
     */
    public static ThreadPoolBuilder poolBuilder(String namePrefix) {
        return ThreadPoolBuilder.create(namePrefix);
    }

    // -------------------------------------------------------------------------
    // 执行辅助
    // -------------------------------------------------------------------------

    /**
     * 在虚拟线程中异步执行，异常仅打日志。
     *
     * @param namePrefix 线程名前缀
     * @param task       任务
     * @return 运行中的虚拟线程
     */
    public static Thread startVirtual(String namePrefix, Runnable task) {
        Objects.requireNonNull(task, "task");
        return Thread.ofVirtual()
                .name(namePrefix == null || namePrefix.isBlank() ? "vt-task" : namePrefix)
                .start(wrap(task));
    }

    /**
     * 并行提交全部任务并收集结果（保持入参顺序）。
     *
     * @param executor 执行器
     * @param tasks    任务列表
     * @param timeout  总等待超时
     * @param <T>      结果类型
     * @return 结果列表
     * @throws TimeoutException     超时
     * @throws ExecutionException   任务失败
     * @throws InterruptedException 等待被中断
     */
    public static <T> List<T> invokeAll(ExecutorService executor,
                                        Collection<? extends Callable<T>> tasks,
                                        Duration timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(tasks, "tasks");
        Objects.requireNonNull(timeout, "timeout");

        List<Future<T>> futures = executor.invokeAll(new ArrayList<>(tasks), timeout.toMillis(), TimeUnit.MILLISECONDS);
        List<T> results = new ArrayList<>(futures.size());
        for (Future<T> future : futures) {
            if (!future.isDone()) {
                throw new TimeoutException("invokeAll 超时: " + timeout);
            }
            if (future.isCancelled()) {
                throw new TimeoutException("invokeAll 任务被取消（通常因超时）: " + timeout);
            }
            results.add(future.get());
        }
        return results;
    }

    /**
     * 带超时执行单个任务；超时取消任务并抛出 {@link TimeoutException}。
     *
     * @param executor 执行器
     * @param task     任务
     * @param timeout  超时
     * @param <T>      结果类型
     * @return 任务结果
     */
    public static <T> T callWithTimeout(ExecutorService executor, Callable<T> task, Duration timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        Future<T> future = executor.submit(task);
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw ex;
        }
    }

    // -------------------------------------------------------------------------
    // 关闭
    // -------------------------------------------------------------------------

    /**
     * 优雅关闭：先 {@code shutdown}，超时后再 {@code shutdownNow}。
     *
     * @param executor 执行器，允许 {@code null}
     * @param timeout  等待在途任务结束的最长时间
     * @return {@code true} 表示在超时前完成关闭
     */
    public static boolean shutdownGracefully(ExecutorService executor, Duration timeout) {
        if (executor == null) {
            return true;
        }
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                return true;
            }
            log.warn("线程池未在 {} 内结束，执行 shutdownNow", timeout);
            executor.shutdownNow();
            return executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            return false;
        }
    }

    /**
     * 安全关闭并在失败时回调（例如打监控）。
     *
     * @param executor     执行器
     * @param timeout      超时
     * @param onNotStopped 未能按时停止时的回调
     */
    public static void shutdownGracefully(ExecutorService executor,
                                          Duration timeout,
                                          Consumer<ExecutorService> onNotStopped) {
        boolean stopped = shutdownGracefully(executor, timeout);
        if (!stopped && onNotStopped != null) {
            onNotStopped.accept(executor);
        }
    }

    /**
     * 打印线程池快照（队列长度、活跃线程等），便于排障。
     *
     * @param name     池名称标签
     * @param executor 线程池
     * @return 可读快照字符串
     */
    public static String poolSnapshot(String name, ThreadPoolExecutor executor) {
        Objects.requireNonNull(executor, "executor");
        return "pool[" + name + "]"
                + " active=" + executor.getActiveCount()
                + " poolSize=" + executor.getPoolSize()
                + " core=" + executor.getCorePoolSize()
                + " max=" + executor.getMaximumPoolSize()
                + " queued=" + executor.getQueue().size()
                + " completed=" + executor.getCompletedTaskCount();
    }

    /**
     * 包装虚拟线程任务，捕获未处理异常并记录日志。
     *
     * @param task 原始任务
     * @return 带异常兜底的 Runnable
     */
    private static Runnable wrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable ex) {
                log.error("虚拟线程任务执行失败", ex);
            }
        };
    }

    /**
     * 校验整型参数为正数。
     *
     * @param value 待校验值
     * @param name  参数名
     * @return 原 value
     */
    private static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " 必须 > 0");
        }
        return value;
    }
}
