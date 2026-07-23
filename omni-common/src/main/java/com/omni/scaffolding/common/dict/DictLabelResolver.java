package com.omni.scaffolding.common.dict;

/**
 * 字典值到展示文本的解析接口。
 *
 * <p>接口位于 common 模块，具体数据源由上层业务模块实现。
 */
@FunctionalInterface
public interface DictLabelResolver {

    /**
     * 解析单个字典值。
     *
     * @param typeCode 字典类型编码
     * @param value    原始字段值
     * @return 中文展示文本；未匹配时返回 {@code null}
     */
    String resolve(String typeCode, Object value);
}
