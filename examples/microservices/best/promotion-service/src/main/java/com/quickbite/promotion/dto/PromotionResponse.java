package com.quickbite.promotion.dto;

import com.quickbite.promotion.domain.Promotion;
import com.quickbite.promotion.domain.PromotionType;

import java.time.Instant;

public record PromotionResponse(
        Long id,
        String code,
        PromotionType type,
        long value,
        long minSubtotalCents,
        Instant validFrom,
        Instant validTo,
        Integer maxRedemptions,
        Integer perUserLimit,
        boolean active
) {
    public static PromotionResponse from(Promotion p) {
        return new PromotionResponse(
                p.getId(),
                p.getCode(),
                p.getType(),
                p.getValue(),
                p.getMinSubtotalCents(),
                p.getValidFrom(),
                p.getValidTo(),
                p.getMaxRedemptions(),
                p.getPerUserLimit(),
                p.isActive());
    }
}
