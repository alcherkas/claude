package com.quickbite.order.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.order.domain.OrderStatus;
import com.quickbite.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Consumes deliveries.events. Maps DeliveryStatusChanged delivery states to order states:
 * PICKED_UP -> PICKED_UP, DELIVERED -> DELIVERED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.deliveries-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onDeliveryEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText();
            if (!"DeliveryStatusChanged".equals(type)) {
                log.debug("Ignoring deliveries.events type={}", type);
                return;
            }
            JsonNode payload = root.path("payload");
            UUID orderId = UUID.fromString(payload.path("orderId").asText());
            String deliveryStatus = payload.path("status").asText();

            OrderStatus mapped = mapDeliveryStatus(deliveryStatus);
            if (mapped == null) {
                log.debug("Delivery status {} for order {} does not map to an order status", deliveryStatus, orderId);
                return;
            }
            orderService.transitionStatus(orderId, mapped);
            log.info("Order {} -> {} after delivery status {}", orderId, mapped, deliveryStatus);
        } catch (Exception e) {
            log.error("Failed to process deliveries.events message; skipping. payload={}", message, e);
        }
    }

    private OrderStatus mapDeliveryStatus(String deliveryStatus) {
        return switch (deliveryStatus) {
            case "PICKED_UP" -> OrderStatus.PICKED_UP;
            case "DELIVERED" -> OrderStatus.DELIVERED;
            default -> null;
        };
    }
}
