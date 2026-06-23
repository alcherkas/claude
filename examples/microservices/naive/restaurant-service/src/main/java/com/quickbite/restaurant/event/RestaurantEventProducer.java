package com.quickbite.restaurant.event;

import com.quickbite.restaurant.domain.Restaurant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes restaurant lifecycle events to the {@code restaurant.events} topic.
 * Kept thin and resilient: failures are logged and never break the request flow.
 */
@Slf4j
@Component
public class RestaurantEventProducer {

    public static final String TOPIC = "restaurant.events";
    public static final String TYPE_UPSERTED = "RestaurantUpserted";
    public static final String TYPE_STATUS_CHANGED = "RestaurantStatusChanged";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RestaurantEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUpserted(Restaurant restaurant) {
        publish(TYPE_UPSERTED, restaurant);
    }

    public void publishStatusChanged(Restaurant restaurant) {
        publish(TYPE_STATUS_CHANGED, restaurant);
    }

    private void publish(String type, Restaurant restaurant) {
        try {
            EventEnvelope<RestaurantEventPayload> envelope =
                    EventEnvelope.of(type, RestaurantEventPayload.from(restaurant));
            kafkaTemplate.send(TOPIC, restaurant.getId().toString(), envelope);
            log.debug("Published {} for restaurant {}", type, restaurant.getId());
        } catch (Exception ex) {
            log.error("Failed to publish {} for restaurant {}: {}",
                    type, restaurant.getId(), ex.getMessage(), ex);
        }
    }
}
