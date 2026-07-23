/**
 * XSS 输入防护：过滤器 + 请求包装 + 字符串清洗。
 *
 * <p>默认对 Query/Form 参数、部分 Header、JSON/文本 Body 做危险片段剥离；
 * 并通过 Spring Security Headers 补充浏览器侧防护（CSP / X-Content-Type-Options 等）。
 *
 * <p>配置前缀：{@code omni.security.xss}。
 */
package com.omni.scaffolding.security.xss;
