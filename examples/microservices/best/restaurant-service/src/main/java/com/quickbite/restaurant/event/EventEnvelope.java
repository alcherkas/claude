package com.quickbite.restaurant.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard QuickBite event envelope: {eventId, type, occurredAt, payload}.
 */
public record EventEnvelope<T>(
        String eventId,
        String type,
        Instant occurredAt,
        T payload
) {
    public static <T> EventEnvelope<T> of(String type, T payload) {
        return new EventEnvelope<>(UUID.randomUUID().toString(), type, Instant.now(), payload);
    }
}
