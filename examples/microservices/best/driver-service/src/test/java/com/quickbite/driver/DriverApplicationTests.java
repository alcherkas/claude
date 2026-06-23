package com.quickbite.driver;

import com.quickbite.driver.client.IdentityClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Context-load smoke test. The identity Feign client is mocked so the context
 * boots without a live dependency, and the datasource points at an in-memory H2
 * (PostgreSQL mode) with Flyway disabled.
 */
@SpringBootTest
class DriverApplicationTests {

    @MockBean
    IdentityClient identityClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.datasource.url",
                () -> "jdbc:h2:mem:driver;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
    }

    @Test
    void contextLoads() {
    }
}
