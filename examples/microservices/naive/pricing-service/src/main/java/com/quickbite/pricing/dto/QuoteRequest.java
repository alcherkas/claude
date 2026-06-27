package com.quickbite.pricing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Public request body for {@code POST /api/pricing/quote}.
 *
 * @param userId       the customer requesting the quote
 * @param restaurantId the restaurant the cart belongs to
 * @param items        the cart line items to price
 * @param promoCode    optional promotion code to apply
 * @param tipCents     optional courier tip in cents (null treated as 0)
 */
public record QuoteRequest(
        @NotNull Long userId,
        @NotNull Long restaurantId,
        @NotEmpty @Valid List<QuoteItem> items,
        String promoCode,
        Long tipCents
) {
    /**
     * A single requested cart line.
     *
     * @param menuItemId the menu item to price (authoritative price fetched from menu-service)
     * @param qty        quantity ordered (must be positive)
     */
    public record QuoteItem(
            @NotNull Long menuItemId,
            @NotNull @jakarta.validation.constraints.Positive Integer qty
    ) {
    }
}
