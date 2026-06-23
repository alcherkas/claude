package com.quickbite.order.event;

import java.time.Instant;
import java.util.UUID;

/** Standard QuickBite event envelope: {eventId,type,occurredAt,payload}. */
public record EventEnvelope<T>(
        UUID eventId,
        String type,
        Instant occurredAt,
        T payload
) {
    public static <T> EventEnvelope<T> of(String type, T payload) {
        return new EventEnvelope<>(UUID.randomUUID(), type, Instant.now(), payload);
    }
}
