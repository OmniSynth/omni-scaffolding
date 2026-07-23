package com.omni.scaffolding.common.dict;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据源属性值写出只读的 {@code 属性名Text} 字段。
 */
final class DictTextBeanPropertyWriter extends BeanPropertyWriter {

    private static final Object RESPONSE_CACHE_KEY = DictTextBeanPropertyWriter.class.getName() + ".cache";

    private final BeanPropertyWriter source;
    private final String typeCode;
    private final DictLabelResolver resolver;

    DictTextBeanPropertyWriter(BeanPropertyWriter source,
                               String outputName,
                               String typeCode,
                               DictLabelResolver resolver) {
        super(source, PropertyName.construct(outputName));
        this.source = source;
        this.typeCode = typeCode;
        this.resolver = resolver;
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider serializers) throws Exception {
        Object value = source.get(bean);
        gen.writeFieldName(getName());
        if (value == null) {
            gen.writeNull();
            return;
        }

        String raw = String.valueOf(value);
        Map<String, String> cache = responseCache(serializers);
        String cacheKey = typeCode + '\0' + raw;
        String label;
        if (cache.containsKey(cacheKey)) {
            label = cache.get(cacheKey);
        } else {
            label = resolver.resolve(typeCode, value);
            cache.put(cacheKey, label);
        }
        if (label == null) {
            gen.writeNull();
        } else {
            gen.writeString(label);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> responseCache(SerializerProvider serializers) {
        Object existing = serializers.getAttribute(RESPONSE_CACHE_KEY);
        if (existing instanceof Map<?, ?> map) {
            return (Map<String, String>) map;
        }
        Map<String, String> created = new HashMap<>();
        serializers.setAttribute(RESPONSE_CACHE_KEY, created);
        return created;
    }
}
