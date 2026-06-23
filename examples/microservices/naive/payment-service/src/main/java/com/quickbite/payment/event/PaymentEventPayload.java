package com.quickbite.payment.event;

import com.quickbite.payment.domain.Payment;

import java.util.UUID;

/** Payload for PaymentCaptured / PaymentFailed / Refunded events on payments.events. */
public record PaymentEventPayload(
        UUID paymentId,
        UUID orderId,
        UUID userId,
        long amountCents,
        String currency,
        String method,
        String status,
        String provider
) {
    public static PaymentEventPayload from(Payment p) {
        return new PaymentEventPayload(
                p.getId(),
                p.getOrderId(),
                p.getUserId(),
                p.getAmountCents(),
                p.getCurrency(),
                p.getMethod().name(),
                p.getStatus().name(),
                p.getProvider());
    }
}
