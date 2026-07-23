/**
 * 基础设施适配层：分布式锁、限流、Kafka 发送等与中间件交互的封装。
 *
 * <p>业务 Service 只依赖本包提供的能力接口 / 组件，不直接散落 Redis Lua / 限流细节。
 * Kafka / Elasticsearch 相关组件仅在对应 {@code omni.*.enabled=true} 时注册。
 */
package com.omni.scaffolding.infra;
