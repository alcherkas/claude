package com.quickbite.cart.dto;

public record CartItemResponse(
        Long itemId,
        Long menuItemId,
        String name,
        int qty,
        long unitPriceCents,
        long lineTotalCents
) {
}
