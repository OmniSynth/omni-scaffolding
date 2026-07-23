package com.omni.scaffolding.common.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 脚手架演示用随机主键发号；生产可换雪花 / 号段。
 *
 * <p>上限控制在 JS {@code Number.MAX_SAFE_INTEGER} 内，避免前端 JSON 数字精度丢失。
 */
public final class IdGenerator {

    /** 小于 Number.MAX_SAFE_INTEGER (9007199254740991) */
    private static final long MAX_SAFE_ID = 9_000_000_000_000_000L;

    private IdGenerator() {
    }

    /**
     * 生成随机主键。
     *
     * @return 正 long，小于 JS {@code Number.MAX_SAFE_INTEGER}
     */
    public static long nextId() {
        return ThreadLocalRandom.current().nextLong(1_000_000L, MAX_SAFE_ID);
    }
}
