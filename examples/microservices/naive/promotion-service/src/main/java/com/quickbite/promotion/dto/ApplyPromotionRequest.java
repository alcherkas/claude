package com.quickbite.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Internal request from pricing/order to reserve a redemption against a promo code.
 */
public record ApplyPromotionRequest(
        @NotBlank String code,
        @NotNull Long userId,
        @PositiveOrZero long subtotalCents,
        @NotNull Long orderId
) {
}
