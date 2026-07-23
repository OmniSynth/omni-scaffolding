package com.omni.scaffolding.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.omni.scaffolding.common.dict.DictLabelResolver;
import com.omni.scaffolding.common.dict.DictTextBeanSerializerModifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册字典文本虚拟字段的 Jackson 扩展。
 */
@Configuration
public class DictTextJacksonConfig {

    /**
     * 创建字典文本序列化模块。
     *
     * <p>未引入字典业务实现时使用空解析器，保持 framework 可独立复用。
     *
     * @param resolverProvider 字典解析器提供者
     * @return Jackson 模块
     */
    @Bean
    public Module dictTextJacksonModule(ObjectProvider<DictLabelResolver> resolverProvider) {
        DictLabelResolver resolver = resolverProvider.getIfAvailable(() -> (typeCode, value) -> null);
        SimpleModule module = new SimpleModule("DictTextModule");
        module.setSerializerModifier(new DictTextBeanSerializerModifier(resolver));
        return module;
    }
}
