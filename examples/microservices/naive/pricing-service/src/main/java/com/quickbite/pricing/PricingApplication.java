package com.quickbite.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * QuickBite pricing-service (port 8086, stateless).
 *
 * <p>Computes an authoritative price quote for a cart: it fetches per-item prices
 * from menu-service, applies a promotion via promotion-service, and layers on
 * delivery / service / tax fees.</p>
 */
@SpringBootApplication
@EnableFeignClients
public class PricingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricingApplication.class, args);
    }
}
