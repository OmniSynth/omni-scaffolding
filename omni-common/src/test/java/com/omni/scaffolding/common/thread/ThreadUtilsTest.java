package com.omni.scaffolding.common.thread;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadUtilsTest {

    @Test
    void virtualExecutor_runsTasks() throws Exception {
        try (ExecutorService executor = ThreadUtils.newVirtualExecutor("ut-vt")) {
            List<Integer> result = ThreadUtils.invokeAll(
                    executor,
                    List.<Callable<Integer>>of(() -> 1, () -> 2, () -> 3),
                    Duration.ofSeconds(5)
            );
            assertThat(result).containsExactly(1, 2, 3);
        }
    }

    @Test
    void cpuPool_builderDefaults() {
        ThreadPoolExecutor pool = ThreadUtils.newCpuPool("ut-cpu");
        try {
            assertThat(pool.getCorePoolSize()).isEqualTo(Runtime.getRuntime().availableProcessors());
            assertThat(pool.getMaximumPoolSize()).isGreaterThanOrEqualTo(pool.getCorePoolSize());
            assertThat(ThreadUtils.poolSnapshot("ut-cpu", pool)).contains("pool[ut-cpu]");
        } finally {
            assertThat(ThreadUtils.shutdownGracefully(pool, Duration.ofSeconds(3))).isTrue();
        }
    }

    @Test
    void poolBuilder_customQueueAndRejection() throws Exception {
        ThreadPoolExecutor pool = ThreadUtils.poolBuilder("ut-custom")
                .corePoolSize(1)
                .maxPoolSize(1)
                .queueCapacity(1)
                .callerRunsWhenRejected()
                .build();
        try {
            AtomicBoolean ran = new AtomicBoolean(false);
            pool.submit(() -> ran.set(true)).get();
            assertThat(ran).isTrue();
        } finally {
            ThreadUtils.shutdownGracefully(pool, Duration.ofSeconds(3));
        }
    }

    @Test
    void startVirtual_setsThreadNamePrefix() throws Exception {
        AtomicBoolean virtual = new AtomicBoolean(false);
        Thread thread = ThreadUtils.startVirtual("ut-start", () -> virtual.set(Thread.currentThread().isVirtual()));
        thread.join(Duration.ofSeconds(3));
        assertThat(thread.getName()).startsWith("ut-start");
        assertThat(virtual).isTrue();
    }

    @Test
    void scheduledPool_canSchedule() throws Exception {
        var scheduler = ThreadUtils.newScheduledPool("ut-sched", 1);
        try {
            AtomicBoolean done = new AtomicBoolean(false);
            scheduler.schedule(() -> done.set(true), 50, java.util.concurrent.TimeUnit.MILLISECONDS).get();
            assertThat(done).isTrue();
        } finally {
            ThreadUtils.shutdownGracefully(scheduler, Duration.ofSeconds(3));
        }
    }
}
