package com.omni.scaffolding.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.config.OmniKafkaProperties;
import com.omni.scaffolding.infra.kafka.KafkaEventPublisher;
import com.omni.scaffolding.modules.demo.kafka.DemoKafkaEvent;
import com.omni.scaffolding.modules.demo.kafka.DemoKafkaPublishRequest;
import com.omni.scaffolding.modules.demo.service.DemoKafkaService;
import com.omni.scaffolding.support.TestRedisConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kafka 集成测试：EmbeddedKafka 验证 {@code omni.kafka.enabled=true} 后收发链路。
 *
 * <p>{@code omni.kafka.enabled} 必须写在 {@code @SpringBootTest(properties)}，
 * 以便 {@code KafkaEnableEnvironmentPostProcessor} 在启动早期读到开关。
 *
 * <p>真实 Broker（如 192.168.6.80:9092）见 {@link DemoKafkaExternalBrokerIT}。
 */
@SpringBootTest(properties = {
        "omni.kafka.enabled=true",
        "omni.kafka.demo-topic=" + DemoKafkaIntegrationTest.TOPIC,
        "spring.kafka.consumer.group-id=omni-scaffolding-embedded",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = DemoKafkaIntegrationTest.TOPIC,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class DemoKafkaIntegrationTest {

    static final String TOPIC = "omni.demo.it.events";

    @Autowired
    private DemoKafkaService demoKafkaService;

    @Autowired
    private KafkaEventPublisher kafkaEventPublisher;

    @Autowired
    private OmniKafkaProperties kafkaProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void kafkaIntegration_publishAndConsume() throws Exception {
        assertThat(kafkaProperties.isEnabled()).isTrue();
        assertThat(kafkaProperties.getDemoTopic()).isEqualTo(TOPIC);

        String message = "embedded-kafka-" + UUID.randomUUID();
        DemoKafkaPublishRequest request = new DemoKafkaPublishRequest();
        request.setType("INTEGRATION_TEST");
        request.setKey("test-key");
        request.setMessage(message);

        try (Consumer<String, String> consumer = createAssertConsumer()) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, TOPIC);

            DemoKafkaEvent sent = demoKafkaService.publish(request);
            assertThat(sent.getMessage()).isEqualTo(message);

            ConsumerRecord<String, String> serviceRecord =
                    KafkaTestUtils.getSingleRecord(consumer, TOPIC, Duration.ofSeconds(15));
            assertThat(serviceRecord.key()).isEqualTo("test-key");
            DemoKafkaEvent received = objectMapper.readValue(serviceRecord.value(), DemoKafkaEvent.class);
            assertThat(received.getType()).isEqualTo("INTEGRATION_TEST");
            assertThat(received.getMessage()).isEqualTo(message);
            assertThat(received.getSentAt()).isNotNull();

            kafkaEventPublisher.publishSync(TOPIC, "direct-key", "{\"ping\":\"pong\"}");
            ConsumerRecord<String, String> directRecord =
                    KafkaTestUtils.getSingleRecord(consumer, TOPIC, Duration.ofSeconds(15));
            assertThat(directRecord.key()).isEqualTo("direct-key");
            assertThat(directRecord.value()).isEqualTo("{\"ping\":\"pong\"}");
        }
    }

    private Consumer<String, String> createAssertConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                "omni-kafka-assert-" + UUID.randomUUID(), "true", embeddedKafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<String, String>(props).createConsumer();
    }
}
