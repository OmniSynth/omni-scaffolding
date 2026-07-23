package com.omni.scaffolding.security.xss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对参数、部分 Header、JSON/文本 Body 做 XSS 清洗的请求包装器。
 *
 * <p>Body 会被缓存以便可重复读取；multipart / 二进制请求不改写正文。
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 不参与 XSS 清洗的请求头（认证、传输层等）。
     */
    private static final Set<String> SKIP_HEADERS = Set.of(
            "authorization",
            "cookie",
            "content-type",
            "content-length",
            "host",
            "transfer-encoding",
            "connection",
            "accept",
            "accept-encoding",
            "accept-language"
    );

    /**
     * 清洗策略。
     */
    private final XssMode mode;

    /**
     * JSON Body 递归清洗用。
     */
    private final ObjectMapper objectMapper;

    /**
     * 缓存并清洗后的请求正文，支持重复读取。
     */
    private final byte[] cachedBody;

    /**
     * 已清洗的 Query / Form 参数表。
     */
    private final Map<String, String[]> sanitizedParameterMap;

    /**
     * @param request      原始请求
     * @param mode         清洗策略
     * @param objectMapper JSON Body 递归清洗用
     * @throws IOException 读取 Body 失败
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request, XssMode mode, ObjectMapper objectMapper)
            throws IOException {
        super(request);
        this.mode = mode == null ? XssMode.STRIP : mode;
        this.objectMapper = objectMapper;
        this.sanitizedParameterMap = sanitizeParameterMap(request.getParameterMap());
        this.cachedBody = cacheAndSanitizeBody(request);
    }

    @Override
    public String getParameter(String name) {
        String[] values = sanitizedParameterMap.get(name);
        return values == null || values.length == 0 ? null : values[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(sanitizedParameterMap);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(sanitizedParameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = sanitizedParameterMap.get(name);
        return values == null ? null : Arrays.copyOf(values, values.length);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (value == null || shouldSkipHeader(name)) {
            return value;
        }
        return XssSanitizer.clean(value, mode);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Enumeration<String> headers = super.getHeaders(name);
        if (headers == null || shouldSkipHeader(name)) {
            return headers;
        }
        List<String> cleaned = Collections.list(headers).stream()
                .map(v -> XssSanitizer.clean(v, mode))
                .toList();
        return Collections.enumeration(cleaned);
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 非异步场景不需要
            }

            @Override
            public int read() {
                return inputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        Charset charset = resolveCharset();
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }

    @Override
    public int getContentLength() {
        return cachedBody.length;
    }

    @Override
    public long getContentLengthLong() {
        return cachedBody.length;
    }

    /**
     * 清洗 Query / Form 参数表，保留键顺序。
     *
     * @param source 原始参数 Map
     * @return 已清洗的参数 Map
     */
    private Map<String, String[]> sanitizeParameterMap(Map<String, String[]> source) {
        Map<String, String[]> cleaned = new LinkedHashMap<>();
        if (source == null) {
            return cleaned;
        }
        source.forEach((key, values) -> {
            if (values == null) {
                cleaned.put(key, null);
                return;
            }
            String[] next = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                next[i] = XssSanitizer.clean(values[i], mode);
            }
            cleaned.put(key, next);
        });
        return cleaned;
    }

    /**
     * 读取并缓存请求 Body，按 Content-Type 选择 JSON 或纯文本清洗策略。
     *
     * @param request 原始请求
     * @return 清洗后的 Body 字节；multipart / 二进制原样返回
     * @throws IOException 读取 Body 失败
     */
    private byte[] cacheAndSanitizeBody(HttpServletRequest request) throws IOException {
        byte[] raw = request.getInputStream().readAllBytes();
        if (raw.length == 0) {
            return raw;
        }
        String contentType = request.getContentType();
        if (!StringUtils.hasText(contentType)) {
            return sanitizeTextBody(raw, resolveCharset(request));
        }
        String lower = contentType.toLowerCase();
        if (lower.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)
                || lower.startsWith(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                || lower.startsWith("application/pdf")
                || lower.startsWith("image/")
                || lower.startsWith("audio/")
                || lower.startsWith("video/")) {
            // 二进制 / 文件流不改写
            return raw;
        }
        Charset charset = resolveCharset(request);
        if (lower.contains("json")) {
            return sanitizeJsonBody(raw, charset);
        }
        return sanitizeTextBody(raw, charset);
    }

    /**
     * 解析 JSON Body 并递归清洗字符串节点；非法 JSON 降级为纯文本清洗。
     *
     * @param raw     原始 Body 字节
     * @param charset 字符集
     * @return 清洗后的 Body 字节
     */
    private byte[] sanitizeJsonBody(byte[] raw, Charset charset) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode cleaned = cleanJsonNode(root);
            return objectMapper.writeValueAsBytes(cleaned);
        } catch (Exception ex) {
            // 非法 JSON 按纯文本清洗，避免绕过
            return sanitizeTextBody(raw, charset);
        }
    }

    /**
     * 递归清洗 JSON 树节点中的文本值。
     *
     * @param node JSON 节点
     * @return 清洗后的节点
     */
    private JsonNode cleanJsonNode(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return node;
        }
        if (node.isTextual()) {
            return TextNode.valueOf(XssSanitizer.clean(node.asText(), mode));
        }
        if (node.isArray()) {
            ArrayNode array = objectMapper.createArrayNode();
            node.forEach(child -> array.add(cleanJsonNode(child)));
            return array;
        }
        if (node.isObject()) {
            ObjectNode object = objectMapper.createObjectNode();
            node.properties().forEach(entry -> object.set(entry.getKey(), cleanJsonNode(entry.getValue())));
            return object;
        }
        return node;
    }

    /**
     * 将纯文本 Body 按字符集解码、清洗后重新编码。
     *
     * @param raw     原始 Body 字节
     * @param charset 字符集
     * @return 清洗后的 Body 字节
     */
    private byte[] sanitizeTextBody(byte[] raw, Charset charset) {
        String text = new String(raw, charset);
        return XssSanitizer.clean(text, mode).getBytes(charset);
    }

    /**
     * 解析当前包装请求的字符编码。
     *
     * @return 字符集，缺省为 UTF-8
     */
    private Charset resolveCharset() {
        return resolveCharset((HttpServletRequest) getRequest());
    }

    /**
     * 从请求解析字符编码。
     *
     * @param request HTTP 请求
     * @return 字符集，无效或未指定时返回 UTF-8
     */
    private static Charset resolveCharset(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (!StringUtils.hasText(encoding)) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (Exception ex) {
            return StandardCharsets.UTF_8;
        }
    }

    /**
     * 判断请求头是否跳过 XSS 清洗（认证、传输层等敏感头）。
     *
     * @param name 请求头名称
     * @return {@code true} 不清洗
     */
    private static boolean shouldSkipHeader(String name) {
        return name != null && SKIP_HEADERS.contains(name.toLowerCase());
    }
}
