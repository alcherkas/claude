package com.quickbite.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * QuickBite notification-service.
 *
 * <p>Pure event consumer (PLATFORM_SPEC §1.1, §3): {@code @KafkaListener}s on
 * {@code orders.events}, {@code payments.events} and {@code deliveries.events}. For each
 * relevant event it renders a template, persists a {@code notification} row and "sends" it
 * (mock — logs the rendered message).</p>
 *
 * <p>It has no synchronous dependencies: per PLATFORM_SPEC §1.1 its dependencies on
 * order/delivery/payment are entirely via Kafka events, so the service holds no Feign
 * clients. The recipient user id is carried on each inbound event payload.</p>
 */
@SpringBootApplication
public class NotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
