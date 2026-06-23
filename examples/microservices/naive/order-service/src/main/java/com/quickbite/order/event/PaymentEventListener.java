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
 * Consumes payments.events. On PaymentCaptured, advances the order to CONFIRMED.
 * Thin + resilient: deserialize failures are logged and skipped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.payments-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText();
            if (!"PaymentCaptured".equals(type)) {
                log.debug("Ignoring payments.events type={}", type);
                return;
            }
            JsonNode payload = root.path("payload");
            UUID orderId = UUID.fromString(payload.path("orderId").asText());
            orderService.transitionStatus(orderId, OrderStatus.CONFIRMED);
            log.info("Order {} marked CONFIRMED after PaymentCaptured", orderId);
        } catch (Exception e) {
            log.error("Failed to process payments.events message; skipping. payload={}", message, e);
        }
    }
}
