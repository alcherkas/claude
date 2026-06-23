package com.quickbite.payment.dto;

import java.util.UUID;

/** Mirror of wallet-service's debit/credit response. */
public record WalletTxnResponse(
        UUID txnId,
        UUID userId,
        long newBalanceCents,
        String currency,
        String status
) {
}
