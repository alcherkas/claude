package com.quickbite.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

/**
 * Context-load smoke test. Boots the full Spring context against H2 + an embedded Kafka broker
 * so the @KafkaListener wiring, JPA and controllers are validated without external infra.
 */
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"orders.events", "payments.events", "deliveries.events"})
class NotificationApplicationTests {

    @Test
    void contextLoads() {
        // Fails if any bean (datasource, Kafka consumer factory, controllers) is misconfigured.
    }
}
