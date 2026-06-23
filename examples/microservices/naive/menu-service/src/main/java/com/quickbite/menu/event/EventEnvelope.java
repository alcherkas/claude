package com.quickbite.menu.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard platform event envelope: {eventId,type,occurredAt,payload}.
 * Serialized as JSON onto the menu.events topic.
 */
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
