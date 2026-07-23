package com.omni.scaffolding.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.api.ErrorCode;
import com.omni.scaffolding.common.exception.BusinessException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 单次 HTTP 请求的流式构建器。
 *
 * <p>通过 {@link HttpClients} 创建，配置 Query / Header / Body / 超时后调用 {@link #execute()}。
 */
public final class HttpRequestBuilder {

    /** HTTP 方法（大写）。 */
    private final String method;

    /** 原始 URL（不含 Query）。 */
    private final String rawUrl;

    /** Query 参数，同名可对应多值。 */
    private final Map<String, List<String>> queryParams = new LinkedHashMap<>();

    /** 请求头。 */
    private final Map<String, String> headers = new LinkedHashMap<>();

    /** TCP 连接超时。 */
    private Duration connectTimeout = HttpClients.DEFAULT_CONNECT_TIMEOUT;

    /** 整次请求超时。 */
    private Duration requestTimeout = HttpClients.DEFAULT_REQUEST_TIMEOUT;

    /** 重定向策略。 */
    private HttpClient.Redirect redirect = HttpClient.Redirect.NORMAL;

    /** JSON 序列化 / 反序列化用 Mapper。 */
    private ObjectMapper objectMapper = HttpClients.defaultObjectMapper();

    /** 自定义 HttpClient，设置后忽略 connectTimeout / redirect。 */
    private HttpClient customClient;

    /** 请求正文字节。 */
    private byte[] bodyBytes;

    /** 请求正文 Content-Type。 */
    private String contentType;

    /**
     * 包内构造，通过 {@link HttpClients} 创建。
     *
     * @param method HTTP 方法
     * @param url    请求 URL
     */
    HttpRequestBuilder(String method, String url) {
        this.method = Objects.requireNonNull(method, "method").toUpperCase();
        this.rawUrl = Objects.requireNonNull(url, "url");
    }

    /**
     * 追加单个 Query 参数（同名可多次调用形成多值）。
     *
     * @param name  参数名
     * @param value 参数值，{@code null} 将写成空串
     * @return this
     */
    public HttpRequestBuilder query(String name, Object value) {
        Objects.requireNonNull(name, "name");
        queryParams.computeIfAbsent(name, k -> new ArrayList<>())
                .add(value == null ? "" : String.valueOf(value));
        return this;
    }

    /**
     * 批量追加 Query 参数。
     *
     * @param params 参数表；值为 {@link Iterable} 时展开为多值
     * @return this
     */
    public HttpRequestBuilder query(Map<String, ?> params) {
        if (params != null) {
            params.forEach((k, v) -> {
                if (v instanceof Iterable<?> iterable) {
                    for (Object item : iterable) {
                        query(k, item);
                    }
                } else {
                    query(k, v);
                }
            });
        }
        return this;
    }

    /**
     * 设置请求头（同名覆盖）。
     *
     * @param name  头名称
     * @param value 头值
     * @return this
     */
    public HttpRequestBuilder header(String name, String value) {
        Objects.requireNonNull(name, "name");
        headers.put(name, value);
        return this;
    }

    /**
     * 批量设置请求头。
     *
     * @param headers 头表
     * @return this
     */
    public HttpRequestBuilder headers(Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(this::header);
        }
        return this;
    }

    /**
     * 设置 {@code Authorization: Bearer ...}。
     *
     * @param token 访问令牌（不含 Bearer 前缀）
     * @return this
     */
    public HttpRequestBuilder bearerToken(String token) {
        return header("Authorization", "Bearer " + token);
    }

    /**
     * 设置 HTTP Basic 认证头。
     *
     * @param username 用户名
     * @param password 密码
     * @return this
     */
    public HttpRequestBuilder basicAuth(String username, String password) {
        String raw = username + ":" + (password == null ? "" : password);
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return header("Authorization", "Basic " + encoded);
    }

    /**
     * TCP 连接超时。
     *
     * <p>与默认值不同时会为本次请求创建独立 {@link HttpClient}。
     *
     * @param timeout 超时时间，必须为正
     * @return this
     */
    public HttpRequestBuilder connectTimeout(Duration timeout) {
        this.connectTimeout = requirePositive(timeout, "connectTimeout");
        return this;
    }

    /**
     * 整次请求超时（含等待响应体），对应 {@link HttpRequest.Builder#timeout(Duration)}。
     *
     * @param timeout 超时时间，必须为正
     * @return this
     */
    public HttpRequestBuilder requestTimeout(Duration timeout) {
        this.requestTimeout = requirePositive(timeout, "requestTimeout");
        return this;
    }

    /**
     * 同时设置连接超时与请求超时。
     *
     * @param connect 连接超时
     * @param request 请求超时
     * @return this
     */
    public HttpRequestBuilder timeouts(Duration connect, Duration request) {
        return connectTimeout(connect).requestTimeout(request);
    }

    /**
     * 重定向策略。
     *
     * @param redirect {@link HttpClient.Redirect}
     * @return this
     */
    public HttpRequestBuilder followRedirects(HttpClient.Redirect redirect) {
        this.redirect = Objects.requireNonNull(redirect, "redirect");
        return this;
    }

    /**
     * 指定本请求使用的 {@link ObjectMapper}（JSON 序列化 / 响应反序列化）。
     *
     * @param objectMapper Jackson Mapper
     * @return this
     */
    public HttpRequestBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        return this;
    }

    /**
     * 使用自定义 {@link HttpClient}（忽略本构建器上的 connectTimeout / redirect 建连参数）。
     *
     * @param client 自定义客户端
     * @return this
     */
    public HttpRequestBuilder client(HttpClient client) {
        this.customClient = Objects.requireNonNull(client, "client");
        return this;
    }

    /**
     * 发送 JSON 字符串，自动设置 {@code Content-Type: application/json; charset=UTF-8}。
     *
     * @param json JSON 文本
     * @return this
     */
    public HttpRequestBuilder jsonBody(String json) {
        return rawBody(json == null ? "" : json, "application/json; charset=UTF-8");
    }

    /**
     * 将对象序列化为 JSON 后发送。
     *
     * @param body 任意可被 Jackson 序列化的对象
     * @return this
     */
    public HttpRequestBuilder jsonBody(Object body) {
        try {
            return jsonBody(objectMapper.writeValueAsString(body));
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "HTTP JSON 序列化失败: " + ex.getMessage());
        }
    }

    /**
     * 发送 {@code application/x-www-form-urlencoded} 表单。
     *
     * @param form 表单字段
     * @return this
     */
    public HttpRequestBuilder formBody(Map<String, ?> form) {
        String encoded = encodeForm(form);
        return rawBody(encoded, "application/x-www-form-urlencoded; charset=UTF-8");
    }

    /**
     * 发送纯文本。
     *
     * @param text 文本内容
     * @return this
     */
    public HttpRequestBuilder textBody(String text) {
        return rawBody(text == null ? "" : text, "text/plain; charset=UTF-8");
    }

    /**
     * 发送 XML 文本。
     *
     * @param xml XML 内容
     * @return this
     */
    public HttpRequestBuilder xmlBody(String xml) {
        return rawBody(xml == null ? "" : xml, "application/xml; charset=UTF-8");
    }

    /**
     * 发送原始字节，并指定 Content-Type。
     *
     * @param body        字节内容
     * @param contentType MIME 类型
     * @return this
     */
    public HttpRequestBuilder bytesBody(byte[] body, String contentType) {
        this.bodyBytes = body == null ? new byte[0] : body;
        this.contentType = contentType;
        return this;
    }

    /**
     * 发送原始字符串正文。
     *
     * @param body        文本
     * @param contentType MIME 类型
     * @return this
     */
    public HttpRequestBuilder rawBody(String body, String contentType) {
        return bytesBody((body == null ? "" : body).getBytes(StandardCharsets.UTF_8), contentType);
    }

    /**
     * 不发送正文（GET/DELETE/HEAD 等场景可省略）。
     *
     * @return this
     */
    public HttpRequestBuilder noBody() {
        this.bodyBytes = null;
        this.contentType = null;
        return this;
    }

    /**
     * 开始构建 multipart/form-data。
     *
     * @return multipart 构建器，完成后调用 {@link MultipartBody#done()}
     */
    public MultipartBody multipart() {
        return new MultipartBody(this);
    }

    /**
     * 由 {@link MultipartBody} 回写 multipart 正文。
     *
     * @param boundary 分隔符
     * @param body     已组装字节
     * @return this
     */
    HttpRequestBuilder applyMultipart(String boundary, byte[] body) {
        return bytesBody(body, "multipart/form-data; boundary=" + boundary);
    }

    /**
     * 执行请求并返回封装结果（不因 4xx/5xx 抛异常）。
     *
     * @return 响应封装
     * @throws BusinessException 连接失败、超时、中断等传输层错误
     */
    public HttpResult execute() {
        try {
            HttpRequest request = buildRequest();
            HttpClient client = resolveClient();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return new HttpResult(response.statusCode(), response.headers(), response.body(), objectMapper);
        } catch (BusinessException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "HTTP 请求被中断: " + method + " " + rawUrl);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "HTTP 请求失败: " + method + " " + rawUrl + ", cause=" + ex.getClass().getSimpleName()
                            + ": " + ex.getMessage());
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "HTTP 请求异常: " + method + " " + rawUrl + ", " + ex.getMessage());
        }
    }

    /**
     * 执行请求并要求 2xx，否则抛异常。
     *
     * @return 成功响应
     * @throws BusinessException 非 2xx 时
     */
    public HttpResult executeOk() {
        return execute().requireSuccess();
    }

    /**
     * 组装 JDK {@link HttpRequest}（含 Header、Body、超时）。
     *
     * @return 可发送的请求对象
     */
    private HttpRequest buildRequest() {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(buildUrl()))
                .timeout(requestTimeout);

        headers.forEach(builder::header);
        if (contentType != null && !headers.containsKey("Content-Type") && !headers.containsKey("content-type")) {
            builder.header("Content-Type", contentType);
        }

        HttpRequest.BodyPublisher publisher = (bodyBytes == null)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(bodyBytes);

        // JDK HttpRequest 对 PATCH 等需用 method(String, BodyPublisher)
        return builder.method(method, publisher).build();
    }

    /**
     * 拼接原始 URL 与 Query 参数。
     *
     * @return 完整请求 URL
     */
    private String buildUrl() {
        if (queryParams.isEmpty()) {
            return rawUrl;
        }
        StringJoiner joiner = new StringJoiner("&");
        queryParams.forEach((name, values) -> {
            for (String value : values) {
                joiner.add(encode(name) + "=" + encode(value));
            }
        });
        String separator = rawUrl.contains("?") ? "&" : "?";
        return rawUrl + separator + joiner;
    }

    /**
     * 选择 HTTP 客户端：自定义实例、共享实例或按超时新建。
     *
     * @return 本次请求使用的 {@link HttpClient}
     */
    private HttpClient resolveClient() {
        if (customClient != null) {
            return customClient;
        }
        if (connectTimeout.equals(HttpClients.DEFAULT_CONNECT_TIMEOUT)
                && redirect == HttpClient.Redirect.NORMAL) {
            return HttpClients.sharedClient();
        }
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(redirect)
                .build();
    }

    /**
     * 将表单 Map 编码为 {@code application/x-www-form-urlencoded} 字符串。
     *
     * @param form 表单字段
     * @return URL 编码后的键值对串
     */
    private static String encodeForm(Map<String, ?> form) {
        if (form == null || form.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("&");
        form.forEach((k, v) -> {
            if (v instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    joiner.add(encode(k) + "=" + encode(item == null ? "" : String.valueOf(item)));
                }
            } else {
                joiner.add(encode(k) + "=" + encode(v == null ? "" : String.valueOf(v)));
            }
        });
        return joiner.toString();
    }

    /**
     * UTF-8 URL 编码单个值。
     *
     * @param value 待编码字符串
     * @return 编码结果
     */
    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 校验超时时间为正数。
     *
     * @param timeout 超时配置
     * @param name    参数名（用于错误提示）
     * @return 原 timeout
     */
    private static Duration requirePositive(Duration timeout, String name) {
        Objects.requireNonNull(timeout, name);
        if (timeout.isNegative() || timeout.isZero()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, name + " 必须为正数");
        }
        return timeout;
    }
}
