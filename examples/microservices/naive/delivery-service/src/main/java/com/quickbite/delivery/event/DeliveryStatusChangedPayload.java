package com.quickbite.delivery.event;

import com.quickbite.delivery.domain.Delivery;
import com.quickbite.delivery.domain.DeliveryStatus;

import java.time.Instant;
import java.util.UUID;

/** Payload of the DeliveryStatusChanged event on deliveries.events. */
public record DeliveryStatusChangedPayload(
        UUID deliveryId,
        UUID orderId,
        UUID userId,
        UUID driverId,
        DeliveryStatus oldStatus,
        DeliveryStatus newStatus,
        Instant changedAt
) {
    public static DeliveryStatusChangedPayload from(Delivery delivery, DeliveryStatus oldStatus) {
        return new DeliveryStatusChangedPayload(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getUserId(),
                delivery.getDriverId(),
                oldStatus,
                delivery.getStatus(),
                Instant.now());
    }
}
