package com.omni.scaffolding.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omni.scaffolding.modules.demo.kafka.DemoKafkaEvent;
import com.omni.scaffolding.modules.demo.kafka.DemoKafkaPublishRequest;
import com.omni.scaffolding.modules.demo.service.DemoKafkaService;
import com.omni.scaffolding.support.TestRedisConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 对接真实 Broker 的 Kafka 验证（默认跳过）。
 *
 * <p>PowerShell：
 * <pre>
 *   $env:OMNI_KAFKA_IT = "true"
 *   $env:KAFKA_HOSTS = "192.168.6.80"
 *   $env:KAFKA_PORT = "9092"
 *   mvn -s .mvn/settings.xml -pl omni-admin -am "-Dtest=DemoKafkaExternalBrokerIT" "-Dsurefire.failIfNoSpecifiedTests=false" test
 * </pre>
 *
 * <p>注意：Broker 的 {@code advertised.listeners} 必须返回本机可达地址；
 * 若元数据里是 {@code localhost}，客户端会改连本机导致 Send failed。
 */
@SpringBootTest(properties = {
        "omni.kafka.enabled=true",
        "omni.kafka.demo-topic=omni.demo.events",
        "omni.kafka.auto-create-topic=false",
        "spring.kafka.producer.acks=1",
        "spring.kafka.producer.properties.request.timeout.ms=10000",
        "spring.kafka.producer.properties.delivery.timeout.ms=15000",
        "spring.kafka.producer.properties.max.block.ms=10000",
        "spring.kafka.consumer.group-id=omni-scaffolding-external-it",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@DirtiesContext
@EnabledIfEnvironmentVariable(named = "OMNI_KAFKA_IT", matches = "true")
class DemoKafkaExternalBrokerIT {

    private static final String BOOTSTRAP = resolveBootstrap();
    private static final String TOPIC = "omni.demo.events";

    @DynamicPropertySource
    static void kafkaBroker(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> BOOTSTRAP);
    }

    @Autowired
    private DemoKafkaService demoKafkaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @BeforeAll
    static void requireReachableBroker() {
        String host = BOOTSTRAP.substring(0, BOOTSTRAP.lastIndexOf(':'));
        int port = Integer.parseInt(BOOTSTRAP.substring(BOOTSTRAP.lastIndexOf(':') + 1));
        Assumptions.assumeTrue(tcpReachable(host, port, 3_000),
                () -> "Kafka Broker 不可达: " + BOOTSTRAP
                        + "。请确认网络/VPN、KAFKA_HOSTS/KAFKA_PORT，以及 Broker advertised.listeners 不是仅 localhost。");
    }

    @Test
    void publishToExternalBroker_thenConsumable() throws Exception {
        assertThat(bootstrapServers).isEqualTo(BOOTSTRAP);
        ensureTopicExists();

        String message = "external-kafka-" + UUID.randomUUID();
        DemoKafkaPublishRequest request = new DemoKafkaPublishRequest();
        request.setType("EXTERNAL_IT");
        request.setKey("external-key");
        request.setMessage(message);

        try (Consumer<String, String> consumer = createConsumer()) {
            consumer.subscribe(List.of(TOPIC));
            consumer.poll(Duration.ofMillis(500));
            if (!consumer.assignment().isEmpty()) {
                consumer.seekToEnd(consumer.assignment());
                consumer.poll(Duration.ofMillis(200));
            }

            demoKafkaService.publish(request);

            ConsumerRecord<String, String> record = awaitRecord(consumer, "external-key", message);
            DemoKafkaEvent received = objectMapper.readValue(record.value(), DemoKafkaEvent.class);
            assertThat(received.getType()).isEqualTo("EXTERNAL_IT");
            assertThat(received.getMessage()).isEqualTo(message);
        }
    }

    private void ensureTopicExists() throws Exception {
        Map<String, Object> props = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient admin = AdminClient.create(props)) {
            Set<String> names = admin.listTopics().names().get(10, TimeUnit.SECONDS);
            if (!names.contains(TOPIC)) {
                admin.createTopics(List.of(new NewTopic(TOPIC, 1, (short) 1)))
                        .all()
                        .get(15, TimeUnit.SECONDS);
            }
        }
    }

    private Consumer<String, String> createConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "omni-kafka-external-assert-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        return new DefaultKafkaConsumerFactory<String, String>(props).createConsumer();
    }

    private ConsumerRecord<String, String> awaitRecord(Consumer<String, String> consumer,
                                                       String key,
                                                       String message) {
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                if (key.equals(record.key()) && record.value().contains(message)) {
                    return record;
                }
            }
        }
        throw new AssertionError("No matching record from " + bootstrapServers + " within timeout");
    }

    private static String resolveBootstrap() {
        String hosts = envOrDefault("KAFKA_HOSTS", "192.168.6.80");
        String port = envOrDefault("KAFKA_PORT", "9092");
        return hosts + ":" + port;
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    private static boolean tcpReachable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
