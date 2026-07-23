package com.omni.scaffolding.common.desensitize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 读取字段上的 {@link Desensitize} 并在写出 JSON 时做掩码。
 *
 * <p>若 {@link DesensitizeContext#isIgnored()} 为 true，则原样输出。
 */
public class DesensitizeSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    /** 脱敏策略，来自 {@link Desensitize#type()}。 */
    private final DesensitizeType type;

    /** {@link DesensitizeType#CUSTOM} 时保留左侧字符数。 */
    private final int prefixKeep;

    /** {@link DesensitizeType#CUSTOM} 时保留右侧字符数。 */
    private final int suffixKeep;

    /** 掩码字符。 */
    private final char maskChar;

    /** 默认构造，供 Jackson 实例化。 */
    public DesensitizeSerializer() {
        this(DesensitizeType.CUSTOM, 0, 0, '*');
    }

    /**
     * @param type       脱敏策略
     * @param prefixKeep 保留左侧字符数
     * @param suffixKeep 保留右侧字符数
     * @param maskChar   掩码字符
     */
    public DesensitizeSerializer(DesensitizeType type, int prefixKeep, int suffixKeep, char maskChar) {
        this.type = type;
        this.prefixKeep = prefixKeep;
        this.suffixKeep = suffixKeep;
        this.maskChar = maskChar;
    }

    /**
     * 根据字段 {@link Desensitize} 注解创建上下文专用序列化器。
     *
     * @param prov     序列化提供者
     * @param property 当前字段属性
     * @return 带注解参数的序列化器，或无注解时返回自身
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        if (property == null) {
            return this;
        }
        Desensitize ann = property.getAnnotation(Desensitize.class);
        if (ann == null) {
            ann = property.getContextAnnotation(Desensitize.class);
        }
        if (ann == null) {
            return this;
        }
        char mask = ann.maskChar() == null || ann.maskChar().isEmpty() ? '*' : ann.maskChar().charAt(0);
        return new DesensitizeSerializer(ann.type(), ann.prefixKeep(), ann.suffixKeep(), mask);
    }

    /**
     * 序列化字段值；{@link DesensitizeContext#isIgnored()} 时原样输出。
     *
     * @param value       字段值
     * @param gen         JSON 生成器
     * @param serializers 序列化提供者
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        String raw = value instanceof String s ? s : String.valueOf(value);
        if (DesensitizeContext.isIgnored()) {
            gen.writeString(raw);
            return;
        }
        gen.writeString(DesensitizeUtils.desensitize(raw, type, prefixKeep, suffixKeep, maskChar));
    }
}
