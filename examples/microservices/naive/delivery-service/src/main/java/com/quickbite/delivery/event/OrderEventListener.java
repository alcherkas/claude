package com.quickbite.delivery.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Consumes orders.events. On OrderStatusChanged -> READY, auto-creates a PENDING delivery.
 * Thin + resilient: deserialize failures are logged and skipped so a stray event does not
 * poison the consumer.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.orders-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText();
            if (!"OrderStatusChanged".equals(type)) {
                log.debug("Ignoring orders.events type={}", type);
                return;
            }
            JsonNode payload = root.path("payload");
            String newStatus = payload.path("newStatus").asText();
            if (!"READY".equals(newStatus)) {
                log.debug("Ignoring OrderStatusChanged to {}", newStatus);
                return;
            }
            UUID orderId = UUID.fromString(payload.path("orderId").asText());
            deliveryService.createPendingForReadyOrder(orderId);
        } catch (Exception e) {
            log.error("Failed to process orders.events message; skipping. payload={}", message, e);
        }
    }
}
