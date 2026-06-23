package com.quickbite.pricing.dto;

/**
 * Body sent to promotion-service {@code POST /internal/promotions/apply} to
 * reserve a redemption of {@code code} for {@code userId} against a subtotal.
 */
public record PromotionApplyRequest(
        String code,
        Long userId,
        Long restaurantId,
        long subtotalCents
) {
}
