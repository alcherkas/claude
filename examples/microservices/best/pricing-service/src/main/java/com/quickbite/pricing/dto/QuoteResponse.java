package com.quickbite.pricing.dto;

import java.util.List;

/**
 * Public response for {@code POST /api/pricing/quote}. All amounts are integer
 * cents in {@code currency}.
 */
public record QuoteResponse(
        long subtotalCents,
        long deliveryFeeCents,
        long serviceFeeCents,
        long taxCents,
        long discountCents,
        long tipCents,
        long totalCents,
        String currency,
        List<LineItem> lineItems
) {
    /**
     * A priced cart line returned to the caller.
     *
     * @param menuItemId    the menu item id
     * @param name          the resolved item name from menu-service
     * @param qty           quantity ordered
     * @param unitPriceCents authoritative unit price in cents
     * @param lineTotalCents unitPriceCents * qty
     */
    public record LineItem(
            long menuItemId,
            String name,
            int qty,
            long unitPriceCents,
            long lineTotalCents
    ) {
    }
}
