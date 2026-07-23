package com.omni.scaffolding.quartz.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class QuartzJobRegistryTest {

    private AnnotationConfigApplicationContext context;
    private QuartzJobRegistry registry;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.registerBean("allowedTasks", AllowedTasks.class);
        context.registerBean(QuartzJobRegistry.class);
        context.refresh();
        registry = context.getBean(QuartzJobRegistry.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void invokesOnlyAnnotatedNoArgAndStringMethods() throws Exception {
        registry.invoke("allowedTasks.ping", null);
        registry.invoke("allowedTasks.echo", " payload ");

        AllowedTasks tasks = context.getBean(AllowedTasks.class);
        assertThat(tasks.pingCount).isEqualTo(1);
        assertThat(tasks.payload).isEqualTo("payload");
        assertThat(registry.registeredTargets())
                .containsExactlyInAnyOrder("allowedTasks.ping", "allowedTasks.echo");
    }

    @Test
    void rejectsUnannotatedBeanMethod() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> registry.invoke("allowedTasks.dangerous", null))
                .withMessageContaining("未注册");
    }

    @Test
    void rejectsParametersForNoArgMethod() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> registry.invoke("allowedTasks.ping", "unexpected"))
                .withMessageContaining("仅支持无参调用");
    }

    static class AllowedTasks {

        private int pingCount;
        private String payload;

        @QuartzInvokable
        public void ping() {
            pingCount++;
        }

        @QuartzInvokable
        public void echo(String value) {
            payload = value;
        }

        public void dangerous() {
            throw new AssertionError("unannotated method must never be invoked");
        }
    }
}
