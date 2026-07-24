/**
 * 字段级数据脱敏（Jackson 序列化时生效）。
 *
 * <h2>使用步骤</h2>
 * <ol>
 *   <li>在响应 DTO 字段上标注 {@link com.omni.scaffolding.common.desensitize.Desensitize}</li>
 *   <li>选择 {@link com.omni.scaffolding.common.desensitize.DesensitizeType} 或自定义保留位数</li>
 *   <li>若某接口需返回明文（如编辑回填），在 Controller 方法上加
 *       {@link com.omni.scaffolding.common.desensitize.WithoutDesensitize}</li>
 * </ol>
 *
 * <h2>约定</h2>
 * <ul>
 *   <li>仅影响 JSON 输出，不改数据库与内存对象</li>
 *   <li>算法见 {@link com.omni.scaffolding.common.util.DesensitizeUtils}</li>
 * </ul>
 */
package com.omni.scaffolding.common.desensitize;
