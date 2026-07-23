/**
 * HTTP 客户端工具：基于 JDK {@link java.net.http.HttpClient}，无第三方 HTTP 依赖。
 *
 * <h2>入口</h2>
 * {@link HttpClients} 提供 GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS 以及自定义方法。
 *
 * <h2>典型用法</h2>
 * <pre>{@code
 * HttpResult result = HttpClients.post("https://api.example.com/users")
 *         .query("dryRun", true)
 *         .bearerToken(token)
 *         .jsonBody(Map.of("name", "omni"))
 *         .connectTimeout(Duration.ofSeconds(3))
 *         .requestTimeout(Duration.ofSeconds(10))
 *         .execute();
 * String body = result.requireSuccess().asString();
 * }</pre>
 *
 * <p>支持 JSON、表单、multipart、原始文本/字节；超时、Header、Query 均可按请求覆盖。
 */
package com.omni.scaffolding.common.http;
