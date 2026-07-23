/**
 * 业务模块聚合包。
 *
 * <h2>双轨持久化约定（强制）</h2>
 * <ol>
 *   <li><b>写操作优先走 JPA</b>（{@code repository} + Entity）：实体生命周期、乐观锁、审计字段</li>
 *   <li><b>复杂读走 MyBatis</b>（{@code mapper} + XML / DTO）：多表 join、动态条件、聚合统计</li>
 *   <li>同一业务表不要两边随意混写，避免事务语义与缓存不一致</li>
 *   <li>JPA Entity 与 MyBatis 查询 DTO 分离，复杂查询结果不要强行复用 Entity</li>
 * </ol>
 */
package com.omni.scaffolding.modules;
