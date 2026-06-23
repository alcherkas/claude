package com.quickbite.review;

import com.quickbite.review.client.OrderClient;
import com.quickbite.review.client.RestaurantClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ReviewApplicationTests {

    // Feign clients require live targets; stub them so the context loads in isolation.
    @MockBean
    @SuppressWarnings("unused")
    private OrderClient orderClient;

    @MockBean
    @SuppressWarnings("unused")
    private RestaurantClient restaurantClient;

    @Test
    void contextLoads() {
    }
}
