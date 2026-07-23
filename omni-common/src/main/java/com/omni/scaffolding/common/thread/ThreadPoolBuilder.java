package com.omni.scaffolding.common.thread;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 平台线程池流式构建器。
 *
 * <p>适合 CPU 密集或需要并发上限的场景；I/O 密集请优先 {@link ThreadUtils#newVirtualExecutor(String)}。
 *
 * <pre>{@code
 * ThreadPoolExecutor pool = ThreadPoolBuilder.create("export")
 *         .corePoolSize(4)
 *         .maxPoolSize(8)
 *         .queueCapacity(200)
 *         .keepAlive(Duration.ofSeconds(60))
 *         .callerRunsWhenRejected()
 *         .build();
 * }</pre>
 */
public final class ThreadPoolBuilder {

    /** 线程名前缀，实际名称为 {@code prefix + 序号}。 */
    private final String namePrefix;

    /** 核心线程数，默认 CPU 核数。 */
    private int corePoolSize = Runtime.getRuntime().availableProcessors();

    /** 最大线程数，默认核心数 × 2。 */
    private int maxPoolSize = corePoolSize * 2;

    /** 有界队列容量，语义见 {@link #queueCapacity(int)}。 */
    private int queueCapacity = 256;

    /** 空闲线程存活时间。 */
    private Duration keepAlive = Duration.ofSeconds(60);

    /** 是否允许核心线程超时回收。 */
    private boolean allowCoreThreadTimeOut = false;

    /** 是否创建 daemon 线程。 */
    private boolean daemon = false;

    /** 队列满时的拒绝策略。 */
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    private ThreadPoolBuilder(String namePrefix) {
        this.namePrefix = Objects.requireNonNull(namePrefix, "namePrefix");
    }

    /**
     * @param namePrefix 线程名前缀
     * @return 构建器
     */
    public static ThreadPoolBuilder create(String namePrefix) {
        return new ThreadPoolBuilder(namePrefix);
    }

    /**
     * 核心线程数。
     *
     * @param corePoolSize 必须 &gt; 0
     * @return this
     */
    public ThreadPoolBuilder corePoolSize(int corePoolSize) {
        this.corePoolSize = requirePositive(corePoolSize, "corePoolSize");
        return this;
    }

    /**
     * 最大线程数。
     *
     * @param maxPoolSize 必须 &gt;= corePoolSize
     * @return this
     */
    public ThreadPoolBuilder maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = requirePositive(maxPoolSize, "maxPoolSize");
        return this;
    }

    /**
     * 有界队列容量。
     *
     * <ul>
     *   <li>{@code 0}：{@link SynchronousQueue}（直接交接，适合突发）</li>
     *   <li>{@code >0}：{@link ArrayBlockingQueue}</li>
     *   <li>{@code <0}：{@link LinkedBlockingQueue} 无界（慎用，可能导致 OOM）</li>
     * </ul>
     *
     * @param queueCapacity 队列容量语义见上文
     * @return this
     */
    public ThreadPoolBuilder queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    /**
     * 空闲线程存活时间。
     *
     * @param keepAlive 存活时间
     * @return this
     */
    public ThreadPoolBuilder keepAlive(Duration keepAlive) {
        this.keepAlive = Objects.requireNonNull(keepAlive, "keepAlive");
        return this;
    }

    /**
     * 是否允许核心线程超时回收。
     *
     * @param allow {@code true} 允许
     * @return this
     */
    public ThreadPoolBuilder allowCoreThreadTimeOut(boolean allow) {
        this.allowCoreThreadTimeOut = allow;
        return this;
    }

    /**
     * 是否创建 daemon 线程。
     *
     * @param daemon {@code true} 为守护线程
     * @return this
     */
    public ThreadPoolBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * 自定义拒绝策略。
     *
     * @param handler 拒绝处理器
     * @return this
     */
    public ThreadPoolBuilder rejectedExecutionHandler(RejectedExecutionHandler handler) {
        this.rejectedExecutionHandler = Objects.requireNonNull(handler, "handler");
        return this;
    }

    /**
     * 队列满时由调用线程执行（背压，常用默认替代 Abort）。
     *
     * @return this
     */
    public ThreadPoolBuilder callerRunsWhenRejected() {
        this.rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        return this;
    }

    /**
     * 队列满时丢弃最老任务。
     *
     * @return this
     */
    public ThreadPoolBuilder discardOldestWhenRejected() {
        this.rejectedExecutionHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
        return this;
    }

    /**
     * 构建线程池。
     *
     * @return 已配置的 {@link ThreadPoolExecutor}
     */
    public ThreadPoolExecutor build() {
        if (maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maxPoolSize 不能小于 corePoolSize");
        }
        BlockingQueue<Runnable> queue = createQueue(queueCapacity);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAlive.toMillis(),
                TimeUnit.MILLISECONDS,
                queue,
                new NamedThreadFactory(namePrefix, false, daemon),
                rejectedExecutionHandler
        );
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        return executor;
    }

    /**
     * 按容量创建线程池工作队列。
     *
     * @param capacity 0 为 SynchronousQueue，&lt;0 为无界 LinkedBlockingQueue，否则有界 ArrayBlockingQueue
     * @return 阻塞队列
     */
    private static BlockingQueue<Runnable> createQueue(int capacity) {
        if (capacity == 0) {
            return new SynchronousQueue<>();
        }
        if (capacity < 0) {
            return new LinkedBlockingQueue<>();
        }
        return new ArrayBlockingQueue<>(capacity);
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
