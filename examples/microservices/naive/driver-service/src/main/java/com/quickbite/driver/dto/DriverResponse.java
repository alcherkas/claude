package com.quickbite.driver.dto;

import com.quickbite.driver.domain.Driver;
import com.quickbite.driver.domain.DriverStatus;
import com.quickbite.driver.domain.Vehicle;
import java.time.Instant;
import java.util.UUID;

/**
 * Public representation of a courier, returned by the {@code /api/drivers} endpoints.
 */
public record DriverResponse(
        UUID id,
        UUID userId,
        String name,
        Vehicle vehicle,
        DriverStatus status,
        Double lat,
        Double lng,
        Instant updatedAt
) {
    public static DriverResponse from(Driver d) {
        return new DriverResponse(
                d.getId(),
                d.getUserId(),
                d.getName(),
                d.getVehicle(),
                d.getStatus(),
                d.getLat(),
                d.getLng(),
                d.getUpdatedAt());
    }
}
