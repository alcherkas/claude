package com.quickbite.order.client;

import com.quickbite.order.dto.RestaurantSummary;
import com.quickbite.order.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RestaurantClientFallback implements RestaurantClient {

    @Override
    public RestaurantSummary getRestaurant(UUID id) {
        throw new DependencyUnavailableException("restaurant-service is unavailable; cannot validate restaurant");
    }
}
