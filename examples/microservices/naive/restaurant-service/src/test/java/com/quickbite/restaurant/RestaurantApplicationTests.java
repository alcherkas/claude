package com.quickbite.restaurant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.quickbite.restaurant.client.IdentityClient;

/**
 * Context-load smoke test. Infrastructure beans that need external systems
 * (Kafka, the identity Feign client) are mocked so the context boots without a
 * live cluster, and the datasource is pointed at an in-memory-ish stub URL with
 * Flyway disabled.
 */
@SpringBootTest
class RestaurantApplicationTests {

    @MockBean
    KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    IdentityClient identityClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.datasource.url",
                () -> "jdbc:h2:mem:restaurant;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
    }

    @Test
    void contextLoads() {
    }
}
