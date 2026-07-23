package com.omni.scaffolding.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.common.util.JsonUtils;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * HTTP 请求入口，基于 JDK {@link HttpClient}。
 *
 * <h2>覆盖的请求形态</h2>
 * <ul>
 *   <li>方法：GET / POST / PUT / PATCH / DELETE / HEAD / OPTIONS / 自定义</li>
 *   <li>正文：JSON、x-www-form-urlencoded、multipart、text、xml、原始字节</li>
 *   <li>控制：Query、Header、Bearer/Basic、连接超时、请求超时、重定向、自定义 Client</li>
 * </ul>
 *
 * <h2>示例</h2>
 * <pre>{@code
 * // GET + Query + 超时
 * String json = HttpClients.get("https://httpbin.org/get")
 *         .query("q", "omni")
 *         .requestTimeout(Duration.ofSeconds(5))
 *         .executeOk()
 *         .asString();
 *
 * // POST JSON
 * HttpClients.post("https://api.example.com/orders")
 *         .bearerToken(token)
 *         .jsonBody(order)
 *         .timeouts(Duration.ofSeconds(3), Duration.ofSeconds(15))
 *         .executeOk();
 *
 * // 表单
 * HttpClients.post(url).formBody(Map.of("username", "a", "password", "b")).execute();
 *
 * // multipart 上传
 * HttpClients.post(url)
 *         .multipart()
 *         .text("bizType", "import")
 *         .file("file", "a.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes)
 *         .done()
 *         .executeOk();
 * }</pre>
 */
public final class HttpClients {

    /**
     * 默认 TCP 连接超时。
     */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * 默认整次请求超时。
     */
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private static final HttpClient SHARED_CLIENT = HttpClient.newBuilder()
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private HttpClients() {
    }

    /**
     * GET 请求。
     *
     * @param url 完整 URL（可再追加 query）
     * @return 请求构建器
     */
    public static HttpRequestBuilder get(String url) {
        return request("GET", url);
    }

    /**
     * POST 请求。
     *
     * @param url 完整 URL
     * @return 请求构建器
     */
    public static HttpRequestBuilder post(String url) {
        return request("POST", url);
    }

    /**
     * PUT 请求。
     *
     * @param url 完整 URL
     * @return 请求构建器
     */
    public static HttpRequestBuilder put(String url) {
        return request("PUT", url);
    }

    /**
     * PATCH 请求。
     *
     * @param url 完整 URL
     * @return 请求构建器
     */
    public static HttpRequestBuilder patch(String url) {
        return request("PATCH", url);
    }

    /**
     * DELETE 请求。
     *
     * @param url 完整 URL
     * @return 请求构建器
     */
    public static HttpRequestBuilder delete(String url) {
        return request("DELETE", url);
    }

    /**
     * HEAD 请求。
     *
     * @param url 完整 URL
     * @return 请求构建器
     */
    public static HttpRequestBuilder head(String url) {
        return request("HEAD", url);
    }

    /**
     * OPTIONS 请求。
     *
     * @param url 完整 URL
     * @return 请求构建器
     */
    public static HttpRequestBuilder options(String url) {
        return request("OPTIONS", url);
    }

    /**
     * 自定义 HTTP 方法。
     *
     * @param method 方法名，如 {@code GET}、{@code POST}
     * @param url    完整 URL
     * @return 请求构建器
     */
    public static HttpRequestBuilder request(String method, String url) {
        return new HttpRequestBuilder(method, url);
    }

    /**
     * 共享的默认 {@link HttpClient}（连接超时 = {@link #DEFAULT_CONNECT_TIMEOUT}）。
     *
     * @return 共享客户端
     */
    public static HttpClient sharedClient() {
        return SHARED_CLIENT;
    }

    /**
     * 默认 Jackson {@link ObjectMapper}，与 {@link JsonUtils#mapper()} 相同实例。
     *
     * @return 共享 Mapper（请勿修改其全局配置）
     */
    public static ObjectMapper defaultObjectMapper() {
        return JsonUtils.mapper();
    }
}
