package com.quickbite.wallet;

import com.quickbite.wallet.client.IdentityClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class WalletApplicationTests {

    // The Feign client requires startup wiring; mock it for the context test.
    @MockitoBean
    IdentityClient identityClient;

    @Test
    void contextLoads() {
        // Verifies the Spring context boots with all beans wired.
    }
}
