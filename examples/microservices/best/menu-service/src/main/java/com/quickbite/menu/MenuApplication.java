package com.quickbite.menu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * QuickBite menu-service (port 8083, database menu_db).
 *
 * <p>Owns {@code MenuItem}s for restaurants. Validates that the owning restaurant
 * exists and is ACTIVE via the restaurant-service {@code /internal} API, and emits
 * {@code MenuItemUpserted} events on the {@code menu.events} Kafka topic.
 */
@SpringBootApplication
@EnableFeignClients
public class MenuApplication {

    public static void main(String[] args) {
        SpringApplication.run(MenuApplication.class, args);
    }
}
