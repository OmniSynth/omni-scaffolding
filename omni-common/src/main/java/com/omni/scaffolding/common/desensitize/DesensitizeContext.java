package com.omni.scaffolding.common.desensitize;

import java.util.function.Supplier;

/**
 * 脱敏开关上下文（ThreadLocal）。
 *
 * <p>由框架拦截器在遇到 {@link WithoutDesensitize} 时开启跳过；也可在非 Web 场景手动调用。
 */
public final class DesensitizeContext {

    /**
     * 忽略脱敏嵌套深度，&gt;0 时跳过掩码。
     */
    private static final ThreadLocal<Integer> IGNORE_DEPTH = ThreadLocal.withInitial(() -> 0);

    private DesensitizeContext() {
    }

    /**
     * 进入忽略脱敏作用域（可嵌套）。
     */
    public static void ignore() {
        IGNORE_DEPTH.set(IGNORE_DEPTH.get() + 1);
    }

    /**
     * 退出一层忽略作用域。
     */
    public static void restore() {
        int depth = IGNORE_DEPTH.get() - 1;
        if (depth <= 0) {
            IGNORE_DEPTH.remove();
        } else {
            IGNORE_DEPTH.set(depth);
        }
    }

    /**
     * 清空（用于请求结束兜底）。
     */
    public static void clear() {
        IGNORE_DEPTH.remove();
    }

    /**
     * 当前是否跳过脱敏。
     *
     * @return {@code true} 处于 {@link WithoutDesensitize} 或手动 ignore 作用域内
     */
    public static boolean isIgnored() {
        return IGNORE_DEPTH.get() > 0;
    }

    /**
     * 在忽略脱敏的作用域内执行。
     *
     * @param supplier 业务逻辑
     * @param <T>      返回值类型
     * @return supplier 的执行结果
     */
    public static <T> T runWithout(Supplier<T> supplier) {
        ignore();
        try {
            return supplier.get();
        } finally {
            restore();
        }
    }

    /**
     * 在忽略脱敏的作用域内执行（无返回值）。
     *
     * @param runnable 业务逻辑
     */
    public static void runWithout(Runnable runnable) {
        ignore();
        try {
            runnable.run();
        } finally {
            restore();
        }
    }
}
