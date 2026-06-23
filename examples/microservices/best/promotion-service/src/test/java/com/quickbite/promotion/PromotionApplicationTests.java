package com.quickbite.promotion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PromotionApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context (JPA, Feign, controllers) wires up cleanly.
    }
}
