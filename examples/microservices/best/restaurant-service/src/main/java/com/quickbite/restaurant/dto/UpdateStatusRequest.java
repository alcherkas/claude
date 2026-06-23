package com.quickbite.restaurant.dto;

import com.quickbite.restaurant.domain.RestaurantStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull RestaurantStatus status
) {
}
