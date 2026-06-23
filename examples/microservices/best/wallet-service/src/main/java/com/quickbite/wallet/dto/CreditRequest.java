package com.quickbite.wallet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request to credit (top up) a user's wallet. */
public record CreditRequest(
        @NotNull @Min(1) Long amountCents,
        @NotBlank String reason
) {
}
