package com.quickbite.wallet.dto;

import java.time.Instant;
import java.util.UUID;

/** A single ledger entry returned alongside the wallet balance after a debit. */
public record WalletTxnResponse(
        Long id,
        UUID userId,
        long deltaCents,
        String reason,
        long balanceCents,
        Instant createdAt
) {
}
