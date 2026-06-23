package com.quickbite.search.client;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resilience4j fallback for {@link RestaurantClient}. Backfill is best-effort: on a 404 or
 * any upstream failure (circuit open, timeout, 5xx) we return {@code null} so the caller can
 * proceed without the enrichment instead of failing the event-processing flow.
 */
@Slf4j
@Component
public class RestaurantClientFallbackFactory implements FallbackFactory<RestaurantClient> {

    @Override
    public RestaurantClient create(Throwable cause) {
        return new RestaurantClient() {
            @Override
            public RestaurantView getRestaurant(UUID id) {
                if (cause instanceof FeignException.NotFound) {
                    log.debug("restaurant-service has no restaurant {} for index backfill", id);
                } else {
                    log.warn("restaurant-service unavailable while backfilling restaurant {}: {}",
                            id, cause.toString());
                }
                return null;
            }
        };
    }
}
