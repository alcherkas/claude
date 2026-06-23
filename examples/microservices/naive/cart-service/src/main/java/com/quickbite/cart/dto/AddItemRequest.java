package com.quickbite.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request to add a menu item to a cart. The unit price and name are NOT trusted
 * from the client — they are re-fetched authoritatively from menu-service.
 */
public record AddItemRequest(
        @NotNull Long menuItemId,
        @NotNull @Min(1) Integer qty
) {
}
