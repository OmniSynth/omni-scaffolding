/**
 * 线程与线程池工具。
 *
 * <h2>与 Spring 装配的分工</h2>
 * <ul>
 *   <li>框架 Bean（{@code virtualThreadExecutor} / {@code cpuBoundExecutor}）见
 *       {@code com.omni.scaffolding.config.VirtualThreadConfig}</li>
 *   <li>本包提供<strong>编程式</strong>创建、命名、优雅关闭等通用能力，可在非 Spring 场景复用</li>
 * </ul>
 *
 * <h2>选用建议（Java 21）</h2>
 * <ul>
 *   <li>I/O 密集：{@link com.omni.scaffolding.common.util.ThreadUtils#newVirtualExecutor(String)}</li>
 *   <li>CPU 密集：{@link com.omni.scaffolding.common.util.ThreadUtils#newCpuPool(String)} 或 {@link ThreadPoolBuilder}</li>
 * </ul>
 */
package com.omni.scaffolding.common.thread;
