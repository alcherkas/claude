package com.quickbite.cart;

import com.quickbite.cart.client.IdentityClient;
import com.quickbite.cart.client.MenuClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class CartApplicationTests {

    // The Feign clients require a live server at startup wiring; mock them for the context test.
    @MockitoBean
    MenuClient menuClient;

    @MockitoBean
    IdentityClient identityClient;

    @Test
    void contextLoads() {
        // Verifies the Spring context boots with all beans wired.
    }
}
