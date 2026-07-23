package com.omni.scaffolding.common.http;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * multipart/form-data 请求体构建器。
 *
 * <p>通过 {@link HttpRequestBuilder#multipart()} 获取，添加部件后调用 {@link #done()} 回到请求构建器。
 */
public final class MultipartBody {

    private final HttpRequestBuilder parent;
    private final String boundary;
    private final List<Part> parts = new ArrayList<>();

    MultipartBody(HttpRequestBuilder parent) {
        this.parent = parent;
        this.boundary = "----OmniBoundary" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 添加文本字段。
     *
     * @param name  字段名
     * @param value 文本值
     * @return this
     */
    public MultipartBody text(String name, String value) {
        parts.add(Part.text(name, value == null ? "" : value));
        return this;
    }

    /**
     * 添加文件字段。
     *
     * @param name        字段名
     * @param filename    文件名
     * @param contentType MIME，如 {@code application/pdf}；为空则 {@code application/octet-stream}
     * @param content     文件字节
     * @return this
     */
    public MultipartBody file(String name, String filename, String contentType, byte[] content) {
        Objects.requireNonNull(content, "content");
        parts.add(Part.file(name, filename, contentType, content));
        return this;
    }

    /**
     * 结束 multipart 配置，回到 {@link HttpRequestBuilder}。
     *
     * @return 请求构建器
     */
    public HttpRequestBuilder done() {
        return parent.applyMultipart(boundary, buildBytes());
    }

    /**
     * 按 RFC 2388 格式组装 multipart Body 字节。
     *
     * @return 完整 multipart 请求体
     */
    private byte[] buildBytes() {
        byte[] lineBreak = "\r\n".getBytes(StandardCharsets.UTF_8);
        List<byte[]> chunks = new ArrayList<>();
        for (Part part : parts) {
            chunks.add(("--" + boundary).getBytes(StandardCharsets.UTF_8));
            chunks.add(lineBreak);
            chunks.add(part.headerBytes());
            chunks.add(lineBreak);
            chunks.add(lineBreak);
            chunks.add(part.content);
            chunks.add(lineBreak);
        }
        chunks.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        chunks.add(lineBreak);

        int size = chunks.stream().mapToInt(c -> c.length).sum();
        byte[] all = new byte[size];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, all, offset, chunk.length);
            offset += chunk.length;
        }
        return all;
    }

    private record Part(String name, String filename, String contentType, byte[] content) {

        static Part text(String name, String value) {
            return new Part(name, null, "text/plain; charset=UTF-8", value.getBytes(StandardCharsets.UTF_8));
        }

        static Part file(String name, String filename, String contentType, byte[] content) {
            String type = (contentType == null || contentType.isBlank())
                    ? "application/octet-stream"
                    : contentType;
            return new Part(name, filename, type, content);
        }

        byte[] headerBytes() {
            StringBuilder sb = new StringBuilder();
            sb.append("Content-Disposition: form-data; name=\"").append(name).append("\"");
            if (filename != null) {
                sb.append("; filename=\"").append(filename).append("\"");
            }
            sb.append("\r\n");
            sb.append("Content-Type: ").append(contentType);
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
}
