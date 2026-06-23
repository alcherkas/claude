package com.quickbite.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * QuickBite search-service.
 *
 * <p>Event-driven read model: consumes {@code restaurant.events} and {@code menu.events}
 * from Kafka to maintain a denormalized {@code search_doc} index, and serves public
 * {@code /api/search} queries.</p>
 *
 * <p>Synchronous dependencies (PLATFORM_SPEC §1.1): restaurant + menu, reached via Feign
 * clients against their {@code /internal/**} endpoints to backfill the index when an event
 * is missed or arrives out of order.</p>
 */
@SpringBootApplication
@EnableFeignClients
public class SearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}
