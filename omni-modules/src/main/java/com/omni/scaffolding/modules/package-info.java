/**
 * 业务模块聚合包（Maven 模块 {@code omni-modules}）。
 *
 * <ul>
 *   <li>{@code modules.system}：用户 / 角色 / 菜单 / 部门 / 字典 / 参数 / 日志 / 任务 / 文件 / 白名单</li>
 *   <li>{@code modules.open}：开放 API 接口目录 / 客户端 Key / IP 白名单与限额</li>
 *   <li>{@code modules.ops}：服务器 / Redis / MySQL 运维</li>
 *   <li>{@code modules.tool.gen}：在线代码生成</li>
 * </ul>
 *
 * <h2>双轨持久化约定（强制）</h2>
 * <ol>
 *   <li><b>写操作优先走 JPA</b>（{@code repository} + Entity）：实体生命周期、乐观锁、审计字段</li>
 *   <li><b>复杂读走 MyBatis</b>（{@code mapper} + XML / DTO）：多表 join、动态条件、聚合统计</li>
 *   <li>同一业务表不要两边随意混写，避免事务语义与缓存不一致</li>
 *   <li>JPA Entity 与 MyBatis 查询 DTO 分离，复杂查询结果不要强行复用 Entity</li>
 *   <li><b>JPA 写后若同事务内立刻 MyBatis 读/写关联</b>，必须 {@code saveAndFlush}（否则未刷盘会导致读空或外键失败）</li>
 * </ol>
 */
package com.omni.scaffolding.modules;
