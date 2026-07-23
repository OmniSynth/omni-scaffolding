/**
 * Omni Scaffolding 启动聚合包（{@code omni-admin} 模块）。
 *
 * <h2>多模块结构（单仓库 / 单体部署）</h2>
 * <ul>
 *   <li>{@code omni-common}：通用模型、异常、审计基类</li>
 *   <li>{@code omni-framework}：Security、虚拟线程、Redis、限流、持久化装配</li>
 *   <li>{@code omni-system}：用户 / 角色 / 权限 / 认证</li>
 *   <li>{@code omni-demo}：双轨持久化演示（可按需移除依赖）</li>
 *   <li>{@code omni-quartz}：Quartz JDBC 集群定时任务</li>
 *   <li>{@code omni-admin}：Spring Boot 启动入口与运行配置</li>
 * </ul>
 *
 * <p>部署产物是一个 fat jar，不是微服务拆分。
 */
package com.omni.scaffolding;
