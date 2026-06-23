package com.quickbite.order.dto;

import java.util.List;
import java.util.UUID;

/** Request body for pricing-service POST /api/pricing/quote. */
public record PricingQuoteRequest(
        UUID userId,
        UUID restaurantId,
        List<QuoteLineItem> items,
        String promoCode
) {
    public record QuoteLineItem(
            UUID menuItemId,
            int qty
    ) {
    }
}
