/**
 * 基础设施适配层：Redis 统一访问、分布式锁、限流、Kafka 发送等与中间件交互的封装。
 *
 * <p>业务 Service 注入 {@code RedisService} / 锁 / 限流组件，不直接散落 {@code StringRedisTemplate}
 * 或 Redis Lua 细节；运维控制台等可通过 {@code RedisService#template()} 取底层模板。
 * Kafka / Elasticsearch 相关组件仅在对应 {@code omni.*.enabled=true} 时注册。
 */
package com.omni.scaffolding.infra;
