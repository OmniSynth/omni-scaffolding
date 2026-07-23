package com.omni.scaffolding.common.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;

import java.net.http.HttpHeaders;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP 响应封装，便于链式读取状态码、头与正文。
 *
 * <p>默认不因 4xx/5xx 抛异常；需要「必须成功」时调用 {@link #requireSuccess()}。
 */
public final class HttpResult {

    private final int statusCode;
    private final HttpHeaders headers;
    private final byte[] body;
    private final ObjectMapper objectMapper;

    /**
     * @param statusCode   HTTP 状态码
     * @param headers      响应头
     * @param body         响应体字节，不会为 {@code null}
     * @param objectMapper JSON 反序列化使用的 Mapper
     */
    HttpResult(int statusCode, HttpHeaders headers, byte[] body, ObjectMapper objectMapper) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body == null ? new byte[0] : body;
        this.objectMapper = objectMapper;
    }

    /**
     * @return HTTP 状态码
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * @return 是否为 2xx
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * @return 原始响应头
     */
    public HttpHeaders headers() {
        return headers;
    }

    /**
     * 读取单个响应头（忽略大小写，取首个值）。
     *
     * @param name 头名称
     * @return 可选值
     */
    public Optional<String> header(String name) {
        return headers.firstValue(name);
    }

    /**
     * @return 响应头 Map 视图
     */
    public Map<String, List<String>> headerMap() {
        return headers.map();
    }

    /**
     * @return 原始响应体字节（不会返回 {@code null}）
     */
    public byte[] asBytes() {
        return body;
    }

    /**
     * 按 UTF-8 解码响应体。
     *
     * @return 响应文本；无正文时为空串
     */
    public String asString() {
        return asString(StandardCharsets.UTF_8);
    }

    /**
     * 按指定字符集解码响应体。
     *
     * @param charset 字符集
     * @return 响应文本
     */
    public String asString(Charset charset) {
        if (body.length == 0) {
            return "";
        }
        return new String(body, charset);
    }

    /**
     * 将 JSON 响应反序列化为指定类型。
     *
     * @param type 目标类型
     * @param <T>  类型参数
     * @return 反序列化结果；正文为空时返回 {@code null}
     * @throws BusinessException JSON 解析失败时
     */
    public <T> T asJson(Class<T> type) {
        return asJson(type, objectMapper);
    }

    /**
     * 将 JSON 响应反序列化为泛型类型（如 {@code List<User>}）。
     *
     * @param typeReference 类型引用
     * @param <T>           类型参数
     * @return 反序列化结果；正文为空时返回 {@code null}
     * @throws BusinessException JSON 解析失败时
     */
    public <T> T asJson(TypeReference<T> typeReference) {
        return asJson(typeReference, objectMapper);
    }

    /**
     * 使用指定 {@link ObjectMapper} 反序列化 JSON。
     *
     * @param type   目标类型
     * @param mapper Jackson Mapper
     * @param <T>    类型参数
     * @return 反序列化结果
     */
    public <T> T asJson(Class<T> type, ObjectMapper mapper) {
        if (body.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(body, type);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "HTTP 响应 JSON 解析失败: " + ex.getMessage());
        }
    }

    /**
     * 使用指定 {@link ObjectMapper} 反序列化泛型 JSON。
     *
     * @param typeReference 类型引用
     * @param mapper        Jackson Mapper
     * @param <T>           类型参数
     * @return 反序列化结果
     */
    public <T> T asJson(TypeReference<T> typeReference, ObjectMapper mapper) {
        if (body.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(body, typeReference);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "HTTP 响应 JSON 解析失败: " + ex.getMessage());
        }
    }

    /**
     * 断言 2xx，否则抛出业务异常（消息中带状态码与正文摘要）。
     *
     * @return {@code this}，便于链式调用
     * @throws BusinessException 非成功状态时
     */
    public HttpResult requireSuccess() {
        if (!isSuccessful()) {
            String snippet = asString();
            if (snippet.length() > 500) {
                snippet = snippet.substring(0, 500) + "...";
            }
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "HTTP 请求失败 status=" + statusCode + ", body=" + snippet);
        }
        return this;
    }
}
