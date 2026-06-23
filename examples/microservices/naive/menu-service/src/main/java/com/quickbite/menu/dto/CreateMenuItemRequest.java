package com.quickbite.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Public create payload for POST /api/menu. The owning restaurant is validated to
 * exist and be ACTIVE via the restaurant-service before the item is persisted.
 */
public record CreateMenuItemRequest(
        @NotNull UUID restaurantId,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @PositiveOrZero long priceCents,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO code") String currency,
        @NotBlank @Size(max = 100) String category,
        boolean available
) {
}
