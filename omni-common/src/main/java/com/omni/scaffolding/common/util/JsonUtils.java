package com.omni.scaffolding.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson 便捷工具：对象 ↔ JSON 字符串 / 树节点 / 类型转换。
 *
 * <p>内置共享 {@link ObjectMapper}（注册 {@link JavaTimeModule}、日期写 ISO 字符串、忽略未知字段）。
 * Spring 容器内的业务 Bean 若需与 Web 层完全一致的配置，仍可注入容器中的 {@code ObjectMapper}；
 * 本工具适合静态上下文、工具方法与非 Spring 代码。
 *
 * <p>请勿修改 {@link #mapper()} 返回实例的全局配置。
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = createMapper();

    private JsonUtils() {
    }

    /**
     * 共享 Mapper（只读使用，勿改全局配置）。
     *
     * @return 预配置的 {@link ObjectMapper}
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * 对象序列化为 JSON 字符串；{@code null} 返回 {@code null}。
     *
     * @param value 待序列化对象
     * @return JSON 字符串，或 null
     */
    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("JSON 序列化失败: " + ex.getOriginalMessage(), ex);
        }
    }

    /**
     * 对象序列化为格式化 JSON；{@code null} 返回 {@code null}。
     *
     * @param value 待序列化对象
     * @return 格式化 JSON 字符串，或 null
     */
    public static String toJsonPretty(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("JSON 序列化失败: " + ex.getOriginalMessage(), ex);
        }
    }

    /**
     * JSON 反序列化为指定类型；空白字符串返回 {@code null}。
     *
     * @param json JSON 文本
     * @param type 目标类型
     * @param <T>  目标泛型
     * @return 反序列化结果，或 null
     */
    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON 反序列化失败: " + ex.getOriginalMessage(), ex);
        }
    }

    /**
     * JSON 反序列化为泛型类型（如 {@code new TypeReference<List<User>>() {}}）；空白返回 {@code null}。
     *
     * @param json JSON 文本
     * @param type 泛型类型引用
     * @param <T>  目标泛型
     * @return 反序列化结果，或 null
     */
    public static <T> T fromJson(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON 反序列化失败: " + ex.getOriginalMessage(), ex);
        }
    }

    /**
     * 解析为 {@link JsonNode}；空白返回 {@code null}。
     *
     * @param json JSON 文本
     * @return 树节点，或 null
     */
    public static JsonNode parseTree(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON 解析失败: " + ex.getOriginalMessage(), ex);
        }
    }

    /**
     * 对象树转换（Map/POJO ↔ POJO），不经过字符串。
     *
     * @param from 源对象
     * @param type 目标类型
     * @param <T>  目标泛型
     * @return 转换结果，或 null
     */
    public static <T> T convert(Object from, Class<T> type) {
        if (from == null) {
            return null;
        }
        return MAPPER.convertValue(from, type);
    }

    /**
     * 对象树转换（支持泛型 TypeReference）。
     *
     * @param from 源对象
     * @param type 泛型类型引用
     * @param <T>  目标泛型
     * @return 转换结果，或 null
     */
    public static <T> T convert(Object from, TypeReference<T> type) {
        if (from == null) {
            return null;
        }
        return MAPPER.convertValue(from, type);
    }

    /**
     * 粗判是否像 JSON 对象/数组（不以严格校验为目的）。
     *
     * @param text 待检测文本
     * @return {@code true} 若以 {@code {} 或 []} 开头
     */
    public static boolean looksLikeJson(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        char c = trimmed.charAt(0);
        return c == '{' || c == '[';
    }

    /**
     * 创建共享 ObjectMapper（注册 JavaTime、ISO 日期、忽略未知字段）。
     *
     * @return 静态工具使用的 {@link ObjectMapper}
     */
    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
}
