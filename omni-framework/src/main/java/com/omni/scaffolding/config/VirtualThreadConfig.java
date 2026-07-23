package com.omni.scaffolding.config;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 虚拟线程与线程池装配。
 *
 * <h2>使用原则</h2>
 * <ul>
 *   <li>I/O 密集（DB / Redis / HTTP）：用虚拟线程，阻塞成本低</li>
 *   <li>CPU 密集（加解密、大批量计算）：必须走 {@link #cpuBoundExecutor()}，避免占满 carrier 线程</li>
 *   <li>请求线程本身已由 {@code spring.threads.virtual.enabled=true} 切换为 per-request VT</li>
 * </ul>
 *
 * <p>Java 21 注意：长时间 {@code synchronized} 可能导致 pinning；热点竞争优先 {@code ReentrantLock}。
 */
@Configuration
public class VirtualThreadConfig implements AsyncConfigurer {

    /**
     * Spring 默认应用任务执行器，改为按任务创建虚拟线程。
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor applicationTaskExecutor(SimpleAsyncTaskExecutorBuilder builder) {
        return builder.virtualThreads(true)
                .threadNamePrefix("vt-app-")
                .build();
    }

    /**
     * 业务侧可显式注入的 VT 执行器，例如：{@code @Async("virtualThreadExecutor")}。
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 有界平台线程池，专供 CPU 密集任务，与虚拟线程隔离。
     */
    @Bean(name = "cpuBoundExecutor")
    public Executor cpuBoundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(cores * 2);
        // 队列有界，防止任务无限堆积拖垮内存
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("cpu-");
        executor.initialize();
        return executor;
    }

    /**
     * 未指定执行器名称时，{@code @Async} 默认也走虚拟线程。
     */
    @Override
    public Executor getAsyncExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
