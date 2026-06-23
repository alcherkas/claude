package com.quickbite.review.client;

import com.quickbite.review.dto.RestaurantSummary;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Restaurant lookup is optional enrichment, so the fallback degrades gracefully
 * by returning null rather than failing the review submission.
 */
@Component
public class RestaurantClientFallback implements RestaurantClient {

    @Override
    public RestaurantSummary getRestaurant(UUID id) {
        return null;
    }
}
