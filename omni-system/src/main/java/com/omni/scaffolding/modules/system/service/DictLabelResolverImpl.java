package com.omni.scaffolding.modules.system.service;

import com.omni.scaffolding.common.dict.DictLabelResolver;
import com.omni.scaffolding.modules.system.dto.DictOptionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 基于系统数据字典的标签解析器。
 *
 * <p>字典选项读取复用 {@link DictService#listOptions(String)} 的 Redis 缓存。
 */
@Component
@RequiredArgsConstructor
public class DictLabelResolverImpl implements DictLabelResolver {

    private final DictService dictService;

    @Override
    public String resolve(String typeCode, Object value) {
        if (value == null || typeCode == null || typeCode.isBlank()) {
            return null;
        }
        String raw = String.valueOf(value);
        return dictService.listOptions(typeCode).stream()
                .filter(option -> raw.equals(option.getValue()))
                .map(DictOptionView::getLabel)
                .findFirst()
                .orElse(null);
    }
}
