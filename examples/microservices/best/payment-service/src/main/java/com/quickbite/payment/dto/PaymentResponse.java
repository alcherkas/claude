package com.quickbite.payment.dto;

import com.quickbite.payment.domain.Payment;
import com.quickbite.payment.domain.PaymentMethod;
import com.quickbite.payment.domain.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

/** Public + internal representation of a payment. */
public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID userId,
        long amountCents,
        long tipCents,
        String currency,
        PaymentMethod method,
        PaymentStatus status,
        String provider,
        Instant createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrderId(),
                p.getUserId(),
                p.getAmountCents(),
                p.getTipCents(),
                p.getCurrency(),
                p.getMethod(),
                p.getStatus(),
                p.getProvider(),
                p.getCreatedAt());
    }
}
