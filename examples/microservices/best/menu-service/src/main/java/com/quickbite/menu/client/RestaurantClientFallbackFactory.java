package com.quickbite.menu.client;

import com.quickbite.menu.dto.RestaurantView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fallback for {@link RestaurantClient}. When restaurant-service is unavailable we
 * cannot confirm the restaurant exists/ACTIVE, so we surface a clear upstream error
 * rather than silently creating an orphan menu item.
 */
@Slf4j
@Component
public class RestaurantClientFallbackFactory implements FallbackFactory<RestaurantClient> {

    @Override
    public RestaurantClient create(Throwable cause) {
        log.warn("restaurant-service call failed, using fallback: {}", cause.toString());
        return new RestaurantClient() {
            @Override
            public RestaurantView getRestaurant(UUID id) {
                throw new RestaurantUnavailableException(
                        "restaurant-service is unavailable; cannot validate restaurant " + id, cause);
            }
        };
    }

    /** Raised when the restaurant dependency cannot be reached. */
    public static class RestaurantUnavailableException extends RuntimeException {
        public RestaurantUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
