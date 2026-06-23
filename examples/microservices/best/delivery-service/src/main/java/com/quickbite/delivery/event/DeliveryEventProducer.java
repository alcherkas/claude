package com.quickbite.delivery.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.delivery.domain.Delivery;
import com.quickbite.delivery.domain.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes DeliveryStatusChanged onto the deliveries.events topic. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventProducer {

    public static final String DELIVERY_STATUS_CHANGED = "DeliveryStatusChanged";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.deliveries-events}")
    private String deliveriesTopic;

    public void publishStatusChanged(Delivery delivery, DeliveryStatus oldStatus) {
        EventEnvelope<DeliveryStatusChangedPayload> envelope =
                EventEnvelope.of(DELIVERY_STATUS_CHANGED, DeliveryStatusChangedPayload.from(delivery, oldStatus));
        String key = delivery.getId().toString();
        try {
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(deliveriesTopic, key, json);
            log.info("Published {} for delivery={} to {}", envelope.type(), key, deliveriesTopic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize {} for delivery={}; skipping", envelope.type(), key, e);
        }
    }
}
