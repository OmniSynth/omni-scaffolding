package com.omni.scaffolding.common.exception;

import com.omni.scaffolding.common.api.ApiResponse;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.trace.TraceContext;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常转换：保证对外始终是 {@link ApiResponse}，并附带 traceId。
 *
 * <p>未知异常只打错误日志、不回传堆栈给客户端，避免信息泄露。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常 → 统一响应，并按错误码映射 HTTP 状态。
     *
     * @param ex 业务异常
     * @return 带 traceId 的失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        // 业务码映射到合适的 HTTP 状态，便于网关 / 监控按状态码统计
        HttpStatus status = switch (ex.getErrorCode()) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.OK;
        };
        return ResponseEntity.status(status)
                .body(ApiResponse.<Void>fail(ex.getErrorCode(), ex.getMessage())
                        .withTraceId(TraceContext.getTraceId()));
    }

    /**
     * 参数校验 / 非法参数 → 400。
     *
     * @param ex 校验或参数异常
     * @return 带首条错误信息的失败响应
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex) {
        String message = switch (ex) {
            case MethodArgumentNotValidException manv -> manv.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(err -> err.getDefaultMessage() == null || err.getDefaultMessage().isBlank()
                            ? ErrorCode.BAD_REQUEST.getMessage()
                            : err.getDefaultMessage())
                    .orElse(ErrorCode.BAD_REQUEST.getMessage());
            case BindException be -> be.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(err -> err.getDefaultMessage() == null || err.getDefaultMessage().isBlank()
                            ? ErrorCode.BAD_REQUEST.getMessage()
                            : err.getDefaultMessage())
                    .orElse(ErrorCode.BAD_REQUEST.getMessage());
            case ConstraintViolationException cve -> cve.getConstraintViolations().stream()
                    .findFirst()
                    .map(v -> v.getMessage() == null || v.getMessage().isBlank()
                            ? ErrorCode.BAD_REQUEST.getMessage()
                            : v.getMessage())
                    .orElse(ErrorCode.BAD_REQUEST.getMessage());
            default -> ex.getMessage() == null ? ErrorCode.BAD_REQUEST.getMessage() : ex.getMessage();
        };
        return ResponseEntity.badRequest()
                .body(ApiResponse.<Void>fail(ErrorCode.BAD_REQUEST, message)
                        .withTraceId(TraceContext.getTraceId()));
    }

    /**
     * Resilience4j 方法级限流 → 429。
     *
     * @param ex 限流异常
     * @return 429 响应
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimit(RequestNotPermitted ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.<Void>fail(ErrorCode.TOO_MANY_REQUESTS)
                        .withTraceId(TraceContext.getTraceId()));
    }

    /**
     * 认证失败 → 401。
     *
     * @param ex Spring Security 认证异常
     * @return 401 响应
     */
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex) {
        String message = ex instanceof BadCredentialsException
                ? "用户名或密码错误"
                : ErrorCode.UNAUTHORIZED.getMessage();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>fail(ErrorCode.UNAUTHORIZED, message)
                        .withTraceId(TraceContext.getTraceId()));
    }

    /**
     * 已认证但无权限 → 403。
     *
     * @param ex 访问拒绝异常
     * @return 403 响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>fail(ErrorCode.FORBIDDEN)
                        .withTraceId(TraceContext.getTraceId()));
    }

    /**
     * 未知异常兜底 → 500，不回传堆栈。
     *
     * @param ex 未捕获异常
     * @return 500 响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>fail(ErrorCode.INTERNAL_ERROR)
                        .withTraceId(TraceContext.getTraceId()));
    }
}
