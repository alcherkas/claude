package com.quickbite.search.event;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Common event envelope used across QuickBite Kafka topics (PLATFORM_SPEC §5):
 * {@code {eventId, type, occurredAt, payload}}. The payload shape varies by {@code type},
 * so it is kept as a raw {@link JsonNode} and mapped lazily by the consumer.
 */
public record EventEnvelope(
        String eventId,
        String type,
        Instant occurredAt,
        JsonNode payload
) {
}
