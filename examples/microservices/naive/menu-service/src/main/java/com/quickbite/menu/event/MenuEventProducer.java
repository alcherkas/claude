package com.quickbite.menu.event;

import com.quickbite.menu.domain.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes {@code MenuItemUpserted} envelopes onto the {@code menu.events} topic.
 * Thin and resilient: a failed publish is logged, never blocks the write path.
 */
@Slf4j
@Component
public class MenuEventProducer {

    public static final String TYPE_MENU_ITEM_UPSERTED = "MenuItemUpserted";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public MenuEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                             @Value("${kafka.topics.menu-events:menu.events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishUpserted(MenuItem item) {
        EventEnvelope<MenuItemUpserted> envelope =
                EventEnvelope.of(TYPE_MENU_ITEM_UPSERTED, MenuItemUpserted.from(item));
        try {
            kafkaTemplate.send(topic, item.getId().toString(), envelope)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish {} for menuItem {}: {}",
                                    TYPE_MENU_ITEM_UPSERTED, item.getId(), ex.toString());
                        } else {
                            log.debug("Published {} for menuItem {} to {}",
                                    TYPE_MENU_ITEM_UPSERTED, item.getId(), topic);
                        }
                    });
        } catch (Exception ex) {
            log.error("Error submitting {} for menuItem {}: {}",
                    TYPE_MENU_ITEM_UPSERTED, item.getId(), ex.toString());
        }
    }
}
