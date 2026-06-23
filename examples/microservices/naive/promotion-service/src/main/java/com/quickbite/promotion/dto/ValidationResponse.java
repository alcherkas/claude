package com.quickbite.promotion.dto;

import com.quickbite.promotion.domain.PromotionType;

/**
 * Result of {@code GET /api/promotions/validate}. When {@code valid} is false,
 * {@code reason} explains why and {@code discountCents} is 0.
 */
public record ValidationResponse(
        boolean valid,
        PromotionType type,
        long discountCents,
        String reason
) {
    public static ValidationResponse valid(PromotionType type, long discountCents) {
        return new ValidationResponse(true, type, discountCents, null);
    }

    public static ValidationResponse invalid(String reason) {
        return new ValidationResponse(false, null, 0L, reason);
    }
}
