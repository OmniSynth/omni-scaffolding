package com.omni.scaffolding.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应信封。
 *
 * <p>{@code code=0} 表示成功；失败时 {@code message} 为可读说明，{@code traceId} 用于排障串联日志。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 业务码：0 成功，其余见 {@link ErrorCode}。
     */
    private int code;

    /**
     * 可读说明。
     */
    private String message;

    /**
     * 业务载荷；失败时通常为 null。
     */
    private T data;

    /**
     * 链路追踪 ID，便于日志排障。
     */
    private String traceId;

    /**
     * 成功响应（带数据）。
     *
     * @param data 业务载荷，可为 null
     * @param <T>  载荷类型
     * @return {@code code=0} 的响应
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data, null);
    }

    /**
     * 成功响应（无数据）。
     *
     * @param <T> 载荷类型
     * @return {@code code=0} 的响应
     */
    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    /**
     * 失败响应（使用错误码默认文案）。
     *
     * @param errorCode 业务错误码
     * @param <T>       载荷类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null, null);
    }

    /**
     * 失败响应（自定义文案）。
     *
     * @param errorCode 业务错误码
     * @param message   可读说明
     * @param <T>       载荷类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getCode(), message, null, null);
    }

    /**
     * 失败响应（自定义数值码）。
     *
     * @param code    业务码
     * @param message 可读说明
     * @param <T>     载荷类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null, null);
    }

    /**
     * 链式补充链路 ID，通常在全局异常处理或响应增强处调用。
     *
     * @param traceId 链路追踪 ID
     * @return this
     */
    public ApiResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}
