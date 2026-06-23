package com.quickbite.wallet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Internal request from payment-service to debit a user's wallet when paying for
 * an order. The order id is recorded as the ledger reason and ties the debit to
 * the originating payment.
 */
public record DebitRequest(
        @NotNull @Min(1) Long amountCents,
        @NotNull Long orderId
) {
}
