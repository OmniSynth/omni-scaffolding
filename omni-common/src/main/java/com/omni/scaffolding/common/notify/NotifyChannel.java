package com.omni.scaffolding.common.notify;

/**
 * 通知通道 SPI。
 *
 * <p>脚手架默认提供日志实现；业务可增加邮件 / 短信 / Webhook 等 Bean，
 * 由 {@code NotifyDispatcher} 统一分发，无需改公告发布主流程。
 */
public interface NotifyChannel {

    /**
     * 通道 id，如 {@code log} / {@code mail} / {@code sms}。
     */
    String channelId();

    /**
     * 是否处理该消息；可按事件类型或通道能力过滤。
     */
    boolean supports(NotifyMessage message);

    /**
     * 发送通知；实现内应自行捕获业务异常，避免拖垮调用方。
     * 分发器也会兜底吞掉异常并记日志。
     */
    void send(NotifyMessage message);
}
