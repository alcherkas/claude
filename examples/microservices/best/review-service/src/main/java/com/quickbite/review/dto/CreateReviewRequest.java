package com.quickbite.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Public request body for POST /api/reviews. */
public record CreateReviewRequest(
        @NotNull UUID orderId,
        @Min(1) @Max(5) int rating,
        @Size(max = 2000) String comment
) {
}
