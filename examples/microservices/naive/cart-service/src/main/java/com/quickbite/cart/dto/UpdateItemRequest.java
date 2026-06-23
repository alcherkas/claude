package com.quickbite.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request to change the quantity of an existing cart item. */
public record UpdateItemRequest(
        @NotNull @Min(1) Integer qty
) {
}
