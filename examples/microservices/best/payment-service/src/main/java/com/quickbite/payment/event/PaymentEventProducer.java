package com.quickbite.payment.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes PaymentCaptured / PaymentFailed / Refunded onto the payments.events topic. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    public static final String PAYMENT_CAPTURED = "PaymentCaptured";
    public static final String PAYMENT_FAILED = "PaymentFailed";
    public static final String REFUNDED = "Refunded";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.payments-events}")
    private String paymentsTopic;

    public void publishPaymentCaptured(Payment payment) {
        send(payment, PAYMENT_CAPTURED);
    }

    public void publishPaymentFailed(Payment payment) {
        send(payment, PAYMENT_FAILED);
    }

    public void publishRefunded(Payment payment) {
        send(payment, REFUNDED);
    }

    private void send(Payment payment, String type) {
        String key = payment.getOrderId().toString();
        EventEnvelope<PaymentEventPayload> envelope = EventEnvelope.of(type, PaymentEventPayload.from(payment));
        try {
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(paymentsTopic, key, json);
            log.info("Published {} for orderId={} to {}", type, key, paymentsTopic);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize {} for orderId={}; skipping", type, key, e);
        }
    }
}
