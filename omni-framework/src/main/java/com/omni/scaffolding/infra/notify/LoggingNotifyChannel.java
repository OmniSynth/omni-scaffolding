package com.omni.scaffolding.infra.notify;

import com.omni.scaffolding.common.notify.NotifyChannel;
import com.omni.scaffolding.common.notify.NotifyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 默认通知通道：写 INFO 日志，便于本地联调与演示扩展点。
 */
@Slf4j
@Component
public class LoggingNotifyChannel implements NotifyChannel {

    @Override
    public String channelId() {
        return "log";
    }

    @Override
    public boolean supports(NotifyMessage message) {
        return message != null && StringUtils.hasText(message.getEvent());
    }

    @Override
    public void send(NotifyMessage message) {
        log.info("notify channel=log event={} bizType={} bizId={} title={}",
                message.getEvent(),
                message.getBizType(),
                message.getBizId(),
                message.getTitle());
        if (StringUtils.hasText(message.getContent())) {
            log.debug("notify content: {}", message.getContent());
        }
    }
}
