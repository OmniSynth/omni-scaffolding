/**
 * 安全认证与鉴权。
 *
 * <p>采用无状态 JWT，便于多实例水平扩展；方法级权限通过 {@code @PreAuthorize} 声明。
 * Token 内携带角色与权限码，避免每个请求都回源查库（权限变更需重新登录或后续做 token 失效策略）。
 *
 * <p>XSS：{@link com.omni.scaffolding.security.xss.XssFilter} 清洗输入；
 * {@link SecurityConfig} 配置 CSP 等安全响应头。
 */
package com.omni.scaffolding.security;
