package com.quickbite.promotion.dto;

import com.quickbite.promotion.domain.PromotionType;

/**
 * Internal response confirming a reserved redemption.
 */
public record ApplyPromotionResponse(
        Long redemptionId,
        PromotionType type,
        long discountCents
) {
}
