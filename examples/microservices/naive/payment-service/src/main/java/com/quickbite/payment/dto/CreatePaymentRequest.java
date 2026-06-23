package com.quickbite.payment.dto;

import com.quickbite.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Public POST /api/payments body. The authoritative amount is fetched from order-service. */
public record CreatePaymentRequest(
        @NotNull UUID orderId,
        @NotNull UUID userId,
        @NotNull PaymentMethod method
) {
}
