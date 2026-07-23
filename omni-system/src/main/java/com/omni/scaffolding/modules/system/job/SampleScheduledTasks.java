package com.omni.scaffolding.modules.system.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 管理台可调用的示例任务 Bean。
 *
 * <ul>
 *   <li>无参：{@code sampleScheduledTasks.ping}</li>
 *   <li>有参：{@code sampleScheduledTasks.echo}，参数为字符串/JSON</li>
 * </ul>
 */
@Slf4j
@Component("sampleScheduledTasks")
public class SampleScheduledTasks {

    /** 无参心跳，调用目标 {@code sampleScheduledTasks.ping}。 */
    public void ping() {
        log.info("[SampleScheduledTasks] ping ok");
    }

    /**
     * 带参回显，调用目标 {@code sampleScheduledTasks.echo}。
     *
     * @param payload 任务参数字符串
     */
    public void echo(String payload) {
        log.info("[SampleScheduledTasks] echo: {}", payload);
    }
}
