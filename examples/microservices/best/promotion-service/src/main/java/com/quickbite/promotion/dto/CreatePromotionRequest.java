package com.quickbite.promotion.dto;

import com.quickbite.promotion.domain.PromotionType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

public record CreatePromotionRequest(
        @NotBlank String code,
        @NotNull PromotionType type,
        @PositiveOrZero long value,
        @PositiveOrZero long minSubtotalCents,
        @NotNull Instant validFrom,
        @NotNull @Future Instant validTo,
        @Positive Integer maxRedemptions,
        @Positive Integer perUserLimit,
        Boolean active
) {
}
