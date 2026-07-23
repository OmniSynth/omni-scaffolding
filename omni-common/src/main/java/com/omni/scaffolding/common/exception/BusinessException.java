package com.omni.scaffolding.common.exception;

import com.omni.scaffolding.common.api.ErrorCode;
import lombok.Getter;

/**
 * 可预期的业务异常，由 {@link GlobalExceptionHandler} 转换为统一响应。
 *
 * <p>Service 层遇到“用户可见、可恢复”的错误时抛出本异常，勿直接吞掉或返回 null。
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 对应 {@link ApiResponse#getCode()} 的业务错误码。
     */
    private final ErrorCode errorCode;

    /**
     * 使用错误码默认文案。
     *
     * @param errorCode 业务错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 使用自定义文案。
     *
     * @param errorCode 业务错误码
     * @param message   可读说明
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 通用业务失败，错误码固定为 {@link ErrorCode#BUSINESS_ERROR}。
     *
     * @param message 可读说明
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCode.BUSINESS_ERROR;
    }
}
