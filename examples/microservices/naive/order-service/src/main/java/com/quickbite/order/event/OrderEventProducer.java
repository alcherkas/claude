package com.quickbite.order.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.order.domain.Order;
import com.quickbite.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes OrderCreated / OrderStatusChanged onto the orders.events topic. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    public static final String ORDER_CREATED = "OrderCreated";
    public static final String ORDER_STATUS_CHANGED = "OrderStatusChanged";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.orders-events}")
    private String ordersTopic;

    public void publishOrderCreated(Order order) {
        send(order.getId().toString(),
                EventEnvelope.of(ORDER_CREATED, OrderCreatedPayload.from(order)));
    }

    public void publishOrderStatusChanged(Order order, OrderStatus oldStatus) {
        send(order.getId().toString(),
                EventEnvelope.of(ORDER_STATUS_CHANGED, OrderStatusChangedPayload.from(order, oldStatus)));
    }

    private void send(String key, EventEnvelope<?> envelope) {
        try {
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(ordersTopic, key, json);
            log.info("Published {} for key={} to {}", envelope.type(), key, ordersTopic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize {} for key={}; skipping", envelope.type(), key, e);
        }
    }
}
