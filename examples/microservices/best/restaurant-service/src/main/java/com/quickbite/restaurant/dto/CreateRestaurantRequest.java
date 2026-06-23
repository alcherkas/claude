package com.quickbite.restaurant.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateRestaurantRequest(
        @NotNull UUID ownerUserId,
        @NotBlank String name,
        @NotBlank String cuisine,
        @NotBlank String addressLine,
        @NotBlank String city,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double lng,
        String openingHours
) {
}
