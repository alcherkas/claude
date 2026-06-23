package com.quickbite.pricing;

import com.quickbite.pricing.client.MenuClient;
import com.quickbite.pricing.client.PromotionClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Context-load smoke test. The Feign clients are mocked so the application
 * context starts without the real menu/promotion services being reachable.
 */
@SpringBootTest
class PricingApplicationTests {

    @MockBean
    MenuClient menuClient;

    @MockBean
    PromotionClient promotionClient;

    @Test
    void contextLoads() {
    }
}
