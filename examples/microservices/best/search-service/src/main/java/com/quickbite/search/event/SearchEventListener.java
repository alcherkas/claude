package com.quickbite.search.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.search.event.payload.MenuItemPayload;
import com.quickbite.search.event.payload.RestaurantPayload;
import com.quickbite.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code restaurant.events} and {@code menu.events} and upserts the search index.
 *
 * <p>Resilient by design (PLATFORM_SPEC §5): a record that fails to deserialize or map is
 * logged and skipped rather than blocking the partition. The envelope {@code type} drives
 * how the payload is interpreted.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEventListener {

    private final ObjectMapper objectMapper;
    private final SearchIndexService indexService;

    @KafkaListener(
            topics = "restaurant.events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onRestaurantEvent(EventEnvelope envelope) {
        if (envelope == null || envelope.payload() == null) {
            log.warn("Discarding empty restaurant event");
            return;
        }
        try {
            String type = envelope.type();
            switch (type == null ? "" : type) {
                case "RestaurantUpserted", "RestaurantStatusChanged" -> {
                    RestaurantPayload payload =
                            objectMapper.treeToValue(envelope.payload(), RestaurantPayload.class);
                    indexService.upsertRestaurant(payload);
                }
                default -> log.debug("Ignoring restaurant event type '{}'", type);
            }
        } catch (Exception ex) {
            log.error("Skipping unprocessable restaurant event {}: {}",
                    envelope.eventId(), ex.getMessage());
        }
    }

    @KafkaListener(
            topics = "menu.events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMenuEvent(EventEnvelope envelope) {
        if (envelope == null || envelope.payload() == null) {
            log.warn("Discarding empty menu event");
            return;
        }
        try {
            String type = envelope.type();
            if ("MenuItemUpserted".equals(type)) {
                MenuItemPayload payload =
                        objectMapper.treeToValue(envelope.payload(), MenuItemPayload.class);
                indexService.upsertMenuItem(payload);
            } else {
                log.debug("Ignoring menu event type '{}'", type);
            }
        } catch (Exception ex) {
            log.error("Skipping unprocessable menu event {}: {}",
                    envelope.eventId(), ex.getMessage());
        }
    }
}
