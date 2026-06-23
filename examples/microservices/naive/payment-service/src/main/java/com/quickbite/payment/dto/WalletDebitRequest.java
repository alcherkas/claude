package com.quickbite.payment.dto;

import java.util.UUID;

/** Body sent to wallet-service POST /internal/wallets/{userId}/debit. */
public record WalletDebitRequest(
        UUID orderId,
        long amountCents,
        String currency,
        String reference
) {
}
