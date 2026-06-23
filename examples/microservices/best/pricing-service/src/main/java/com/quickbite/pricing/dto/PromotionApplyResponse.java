package com.quickbite.pricing.dto;

/**
 * Result of promotion-service {@code POST /internal/promotions/apply}.
 *
 * @param valid           whether the code was accepted for this user/subtotal
 * @param discountCents   the discount to deduct from the subtotal (cents)
 * @param freeDelivery    when true the delivery fee is waived
 * @param reason          human-readable reason when {@code valid} is false
 */
public record PromotionApplyResponse(
        boolean valid,
        long discountCents,
        boolean freeDelivery,
        String reason
) {
    public static PromotionApplyResponse none() {
        return new PromotionApplyResponse(false, 0L, false, "no promotion applied");
    }
}
