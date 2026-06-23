package com.quickbite.search;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

/**
 * Context-load smoke test. Boots the full Spring context against H2 + an embedded Kafka
 * broker so the @KafkaListener wiring is validated without external infrastructure.
 */
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"restaurant.events", "menu.events"})
class SearchApplicationTests {

    @Test
    void contextLoads() {
        // Fails if any bean (datasource, Kafka consumer factory, controllers) is misconfigured.
    }
}
