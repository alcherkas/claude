package com.quickbite.order.dto;

import java.util.List;
import java.util.UUID;

/** Response body from pricing-service POST /api/pricing/quote. */
public record PricingQuoteResponse(
        long subtotalCents,
        long deliveryFeeCents,
        long serviceFeeCents,
        long taxCents,
        long discountCents,
        long totalCents,
        String currency,
        List<PricedLineItem> lineItems
) {
    public record PricedLineItem(
            UUID menuItemId,
            String name,
            int qty,
            long unitPriceCents,
            long lineTotalCents
    ) {
    }
}
