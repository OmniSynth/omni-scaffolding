/**
 * 开放 API 业务域。
 *
 * <ul>
 *   <li>管理端：接口目录 / 客户端 Key / IP 白名单 / 日限额与 QPS（JWT + 权限码）</li>
 *   <li>开放端：{@code /api/open/**} 由 framework {@code OpenApiAuthFilter} + 本包
 *       {@code OpenApiAccessGuardImpl} 按 API Key 鉴权</li>
 * </ul>
 */
package com.omni.scaffolding.modules.open;
