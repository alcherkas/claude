package com.quickbite.delivery.dto;

import java.time.Instant;
import java.util.UUID;

/** A single courier location reading on a delivery's tracking trail. */
public record TrackingPointResponse(
        UUID id,
        double lat,
        double lng,
        Instant at
) {
}
