package com.omni.scaffolding.common.trace;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 请求链路 ID 工具。
 *
 * <p>基于 MDC 存储，日志 pattern 中通过 {@code %X{traceId}} 输出。
 * 注意：虚拟线程场景下应避免滥用 ThreadLocal；MDC 由框架在请求边界清理（见 {@link TraceIdFilter}）。
 */
public final class TraceContext {

    /** MDC 键名，日志 pattern 使用 {@code %X{traceId}}。 */
    public static final String TRACE_ID_KEY = "traceId";

    /** HTTP 响应 / 请求头名，便于客户端串联排障。 */
    public static final String TRACE_HEADER = "X-Trace-Id";

    private TraceContext() {
    }

    /**
     * 读取当前线程 MDC 中的链路 ID。
     *
     * @return traceId，未设置时返回 null
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 优先使用上游传入的 TraceId；缺失则生成新的，保证全链路可追踪。
     *
     * @param incoming 上游请求头或上下文中的 traceId，可为空
     * @return 最终写入 MDC 的 traceId
     */
    public static String ensureTraceId(String incoming) {
        String traceId = (incoming == null || incoming.isBlank())
                ? UUID.randomUUID().toString().replace("-", "")
                : incoming.trim();
        MDC.put(TRACE_ID_KEY, traceId);
        return traceId;
    }

    /** 清除当前线程 MDC 中的 traceId（请求结束兜底）。 */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
