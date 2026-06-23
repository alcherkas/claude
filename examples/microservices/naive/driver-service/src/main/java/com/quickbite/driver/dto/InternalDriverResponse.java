package com.quickbite.driver.dto;

import com.quickbite.driver.domain.Driver;
import com.quickbite.driver.domain.DriverStatus;
import com.quickbite.driver.domain.Vehicle;
import java.util.UUID;

/**
 * Compact courier view served on {@code /internal/drivers/**} for delivery-service.
 */
public record InternalDriverResponse(
        UUID id,
        UUID userId,
        String name,
        Vehicle vehicle,
        DriverStatus status,
        Double lat,
        Double lng
) {
    public static InternalDriverResponse from(Driver d) {
        return new InternalDriverResponse(
                d.getId(),
                d.getUserId(),
                d.getName(),
                d.getVehicle(),
                d.getStatus(),
                d.getLat(),
                d.getLng());
    }
}
