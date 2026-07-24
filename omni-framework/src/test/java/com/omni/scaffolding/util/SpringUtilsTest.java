package com.omni.scaffolding.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringUtilsTest {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void getBeanFromStaticHelper() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        DemoService bean = SpringUtils.getBean(DemoService.class);
        assertThat(bean.hello()).isEqualTo("ok");
        assertThat(SpringUtils.getBean("demoService", DemoService.class).hello()).isEqualTo("ok");
        assertThat(SpringUtils.containsBean("demoService")).isTrue();
        assertThat(SpringUtils.getBeansOfType(DemoService.class)).containsKey("demoService");
    }

    @Test
    void requireContextBeforeInit() {
        SpringUtils holder = new SpringUtils();
        holder.destroy();
        assertThatThrownBy(() -> SpringUtils.getBean(DemoService.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("尚未初始化");
    }

    @Configuration
    static class TestConfig {
        @Bean
        SpringUtils springUtils() {
            return new SpringUtils();
        }

        @Bean
        DemoService demoService() {
            return new DemoService();
        }
    }

    static class DemoService {
        String hello() {
            return "ok";
        }
    }
}
