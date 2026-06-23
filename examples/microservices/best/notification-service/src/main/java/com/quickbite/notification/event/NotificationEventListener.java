package com.quickbite.notification.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.quickbite.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Consumes the three inbound topics ({@code orders.events}, {@code payments.events},
 * {@code deliveries.events}) and turns relevant events into rendered + persisted notifications.
 *
 * <p>Resilient by design (PLATFORM_SPEC §5): a record that fails to deserialize or map is logged
 * and skipped rather than blocking the partition. The envelope {@code type} selects the template.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${quickbite.topics.orders-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderEvent(EventEnvelope envelope) {
        handle("orders.events", envelope, type -> switch (type) {
            case "OrderCreated" -> "order.created";
            case "OrderStatusChanged" -> "order.status_changed";
            default -> null;
        });
    }

    @KafkaListener(
            topics = "${quickbite.topics.payments-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentEvent(EventEnvelope envelope) {
        handle("payments.events", envelope, type -> switch (type) {
            case "PaymentCaptured" -> "payment.captured";
            case "PaymentFailed" -> "payment.failed";
            case "Refunded" -> "payment.refunded";
            default -> null;
        });
    }

    @KafkaListener(
            topics = "${quickbite.topics.deliveries-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDeliveryEvent(EventEnvelope envelope) {
        handle("deliveries.events", envelope, type -> switch (type) {
            case "DeliveryStatusChanged" -> "delivery.status_changed";
            default -> null;
        });
    }

    /**
     * Common path: validate the envelope, map the event type to a template key, extract the
     * recipient user id and hand off to the service. Any failure is logged and the record skipped.
     */
    private void handle(String topic, EventEnvelope envelope, TemplateResolver resolver) {
        if (envelope == null || envelope.payload() == null) {
            log.warn("Discarding empty event on {}", topic);
            return;
        }
        try {
            String type = envelope.type() == null ? "" : envelope.type();
            String template = resolver.resolve(type);
            if (template == null) {
                log.debug("Ignoring {} event type '{}'", topic, type);
                return;
            }
            JsonNode payload = envelope.payload();
            UUID userId = extractUserId(payload);
            if (userId == null) {
                log.warn("{} event '{}' (id={}) has no userId; skipping",
                        topic, type, envelope.eventId());
                return;
            }
            notificationService.handleEvent(userId, template, type, payload);
        } catch (Exception ex) {
            log.error("Skipping unprocessable {} event {}: {}",
                    topic, envelope.eventId(), ex.getMessage());
        }
    }

    private UUID extractUserId(JsonNode payload) {
        JsonNode node = payload.get("userId");
        if (node == null || node.isNull() || node.asText().isBlank()) {
            return null;
        }
        return UUID.fromString(node.asText());
    }

    @FunctionalInterface
    private interface TemplateResolver {
        String resolve(String type);
    }
}
