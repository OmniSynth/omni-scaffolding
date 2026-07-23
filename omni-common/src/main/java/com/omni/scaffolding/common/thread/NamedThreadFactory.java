package com.omni.scaffolding.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 带业务前缀的 {@link ThreadFactory}，便于日志与监控区分线程池。
 *
 * <p>同时支持平台线程与虚拟线程（由构造参数 {@code virtual} 决定）。
 */
public final class NamedThreadFactory implements ThreadFactory {

    /** 线程名前缀，末尾自动补 {@code -}。 */
    private final String namePrefix;

    /** {@code true} 时创建虚拟线程。 */
    private final boolean virtual;

    /** 是否 daemon（平台线程生效；虚拟线程始终为 daemon）。 */
    private final boolean daemon;

    /** 线程序号自增器。 */
    private final AtomicInteger sequence = new AtomicInteger(1);

    /**
     * 创建平台线程工厂（非 daemon）。
     *
     * @param namePrefix 线程名前缀，实际名称为 {@code prefix + 序号}
     */
    public NamedThreadFactory(String namePrefix) {
        this(namePrefix, false, false);
    }

    /**
     * @param namePrefix 线程名前缀
     * @param virtual    {@code true} 时创建虚拟线程
     * @param daemon     是否 daemon（仅平台线程有意义；虚拟线程同样可设置）
     */
    public NamedThreadFactory(String namePrefix, boolean virtual, boolean daemon) {
        if (namePrefix == null || namePrefix.isBlank()) {
            throw new IllegalArgumentException("namePrefix 不能为空");
        }
        this.namePrefix = namePrefix.endsWith("-") ? namePrefix : namePrefix + "-";
        this.virtual = virtual;
        this.daemon = daemon;
    }

    /**
     * 创建新线程。
     *
     * @param runnable 任务
     * @return 平台线程或虚拟线程
     */
    @Override
    public Thread newThread(Runnable runnable) {
        String name = namePrefix + sequence.getAndIncrement();
        // 虚拟线程始终为 daemon，不允许 setDaemon(false)
        if (virtual) {
            return Thread.ofVirtual().name(name).unstarted(runnable);
        }
        return Thread.ofPlatform().name(name).daemon(daemon).unstarted(runnable);
    }
}
