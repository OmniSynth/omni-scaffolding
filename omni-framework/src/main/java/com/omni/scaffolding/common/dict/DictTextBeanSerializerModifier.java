package com.omni.scaffolding.common.dict;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 为带 {@link DictText} 的属性追加虚拟文本属性。
 */
public class DictTextBeanSerializerModifier extends BeanSerializerModifier {

    private final DictLabelResolver resolver;

    public DictTextBeanSerializerModifier(DictLabelResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        List<BeanPropertyWriter> result = new ArrayList<>(beanProperties);
        Set<String> propertyNames = new HashSet<>();
        beanProperties.forEach(property -> propertyNames.add(property.getName()));

        for (BeanPropertyWriter property : beanProperties) {
            DictText annotation = property.getAnnotation(DictText.class);
            if (annotation == null) {
                annotation = property.getContextAnnotation(DictText.class);
            }
            if (annotation == null) {
                continue;
            }
            if (annotation.value().isBlank()) {
                throw new IllegalStateException("@DictText 字典类型编码不能为空: "
                        + beanDesc.getBeanClass().getName() + "." + property.getName());
            }

            String outputName = property.getName() + "Text";
            if (!propertyNames.add(outputName)) {
                throw new IllegalStateException("@DictText 生成字段与现有属性重名: "
                        + beanDesc.getBeanClass().getName() + "." + outputName);
            }
            result.add(new DictTextBeanPropertyWriter(property, outputName, annotation.value().trim(), resolver));
        }
        return result;
    }
}
