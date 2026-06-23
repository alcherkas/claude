package com.quickbite.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * QuickBite identity-service.
 *
 * <p>Foundational service (no sync dependencies). Owns user accounts and issues
 * HS256 JWTs that the gateway and every other service trust via the shared secret.
 */
@SpringBootApplication
public class IdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }
}
