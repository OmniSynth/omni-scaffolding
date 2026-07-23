package com.omni.scaffolding.infra.notify;

import com.omni.scaffolding.common.notify.NotifyChannel;
import com.omni.scaffolding.common.notify.NotifyEvents;
import com.omni.scaffolding.common.notify.NotifyMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class NotifyDispatcherTest {

    @Test
    void dispatchesToSupportingChannelsAndSwallowsFailures() {
        AtomicInteger ok = new AtomicInteger();
        List<NotifyChannel> channels = new ArrayList<>();
        channels.add(new NotifyChannel() {
            @Override
            public String channelId() {
                return "ok";
            }

            @Override
            public boolean supports(NotifyMessage message) {
                return true;
            }

            @Override
            public void send(NotifyMessage message) {
                ok.incrementAndGet();
            }
        });
        channels.add(new NotifyChannel() {
            @Override
            public String channelId() {
                return "boom";
            }

            @Override
            public boolean supports(NotifyMessage message) {
                return true;
            }

            @Override
            public void send(NotifyMessage message) {
                throw new IllegalStateException("boom");
            }
        });
        channels.add(new NotifyChannel() {
            @Override
            public String channelId() {
                return "skip";
            }

            @Override
            public boolean supports(NotifyMessage message) {
                return false;
            }

            @Override
            public void send(NotifyMessage message) {
                ok.incrementAndGet();
            }
        });

        NotifyDispatcher dispatcher = new NotifyDispatcher(channels);
        dispatcher.dispatch(NotifyMessage.of(NotifyEvents.NOTICE_PUBLISHED, "t", "c", "ANNOUNCE", 1L));
        assertThat(ok.get()).isEqualTo(1);
    }
}
