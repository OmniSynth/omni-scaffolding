/**
 * Excel 导入导出基础设施。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>用注解声明列名、顺序、下拉、必填等元数据，避免业务代码手写 POI</li>
 *   <li>提供模板下载、数据导出、文件导入三类能力</li>
 * </ul>
 *
 * <h2>使用步骤</h2>
 * <ol>
 *   <li>定义 DTO，类上可选 {@link ExcelSheet}，字段上标注 {@link ExcelColumn}</li>
 *   <li>模板：{@link ExcelUtils#writeTemplate(java.io.OutputStream, Class)}</li>
 *   <li>导出：{@link ExcelUtils#write(java.io.OutputStream, Class, java.util.List)}</li>
 *   <li>导入：{@link ExcelUtils#read(java.io.InputStream, Class)}</li>
 * </ol>
 *
 * <h2>约定</h2>
 * <ul>
 *   <li>仅处理标注了 {@link ExcelColumn} 的字段；未标注字段忽略</li>
 *   <li>导入按表头列名匹配，不依赖物理列序；必填列表头缺失或单元格为空时抛业务异常</li>
 *   <li>本包无 Web 依赖，Controller 自行写入 {@code HttpServletResponse} 流即可</li>
 * </ul>
 */
package com.omni.scaffolding.common.excel;
