package com.omni.scaffolding.infra.notify;

import com.omni.scaffolding.common.notify.NotifyChannel;
import com.omni.scaffolding.common.notify.NotifyMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通知分发：将消息推给所有 {@link NotifyChannel} Bean。
 *
 * <p>单通道失败不影响其它通道与业务主流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyDispatcher {

    private final List<NotifyChannel> channels;

    /**
     * 分发消息；无通道时直接返回。
     */
    public void dispatch(NotifyMessage message) {
        if (message == null || channels == null || channels.isEmpty()) {
            return;
        }
        for (NotifyChannel channel : channels) {
            try {
                if (!channel.supports(message)) {
                    continue;
                }
                channel.send(message);
            } catch (Exception ex) {
                log.warn("Notify channel [{}] failed for event={}: {}",
                        channel.channelId(), message.getEvent(), ex.getMessage());
            }
        }
    }
}
