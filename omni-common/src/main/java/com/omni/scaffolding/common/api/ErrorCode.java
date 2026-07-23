package com.omni.scaffolding.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务与协议层错误码。
 *
 * <p>0 成功；4xx/5xx 对齐常见 HTTP 语义；1000+ 预留给领域业务错误扩展。
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "系统内部错误"),
    BUSINESS_ERROR(1000, "业务处理失败");

    /** HTTP/业务层数值码。 */
    private final int code;

    /** 默认可读说明。 */
    private final String message;
}
