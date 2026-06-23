package com.quickbite.wallet.dto;

import java.time.Instant;
import java.util.UUID;

/** Public + internal view of a user's wallet. */
public record WalletResponse(
        UUID userId,
        long balanceCents,
        String currency,
        Instant updatedAt
) {
}
