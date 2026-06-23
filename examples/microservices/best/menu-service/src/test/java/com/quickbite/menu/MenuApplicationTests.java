package com.quickbite.menu;

import com.quickbite.menu.client.RestaurantClient;
import com.quickbite.menu.event.MenuEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

/**
 * Context-load smoke test. External infra (Kafka producer, Feign client) is mocked
 * and the datasource auto-configuration is disabled so the test runs without Postgres.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class MenuApplicationTests {

    @MockBean
    private MenuEventProducer menuEventProducer;

    @MockBean
    private RestaurantClient restaurantClient;

    @Test
    void contextLoads() {
        // Verifies the Spring context wires up with all beans present.
    }
}
