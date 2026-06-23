package com.quickbite.menu.service;

import com.quickbite.menu.client.RestaurantClient;
import com.quickbite.menu.domain.MenuItem;
import com.quickbite.menu.dto.CreateMenuItemRequest;
import com.quickbite.menu.dto.RestaurantView;
import com.quickbite.menu.event.MenuEventProducer;
import com.quickbite.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository repository;
    private final RestaurantClient restaurantClient;
    private final MenuEventProducer eventProducer;

    /**
     * Creates a menu item after confirming the owning restaurant exists and is ACTIVE
     * (via restaurant-service /internal). Emits {@code MenuItemUpserted}.
     */
    @Transactional
    public MenuItem create(CreateMenuItemRequest request) {
        validateActiveRestaurant(request.restaurantId());

        MenuItem item = MenuItem.builder()
                .restaurantId(request.restaurantId())
                .name(request.name())
                .description(request.description())
                .priceCents(request.priceCents())
                .currency(request.currency().toUpperCase())
                .category(request.category())
                .available(request.available())
                .createdAt(Instant.now())
                .build();

        MenuItem saved = repository.save(item);
        eventProducer.publishUpserted(saved);
        log.info("Created menuItem {} for restaurant {}", saved.getId(), saved.getRestaurantId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<MenuItem> listByRestaurant(UUID restaurantId) {
        return repository.findByRestaurantIdOrderByCategoryAscNameAsc(restaurantId);
    }

    @Transactional(readOnly = true)
    public MenuItem get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu item not found: " + id));
    }

    /**
     * Updates availability and re-emits {@code MenuItemUpserted} so downstream read
     * models (search) stay in sync.
     */
    @Transactional
    public MenuItem updateAvailability(UUID id, boolean available) {
        MenuItem item = get(id);
        item.setAvailable(available);
        MenuItem saved = repository.save(item);
        eventProducer.publishUpserted(saved);
        log.info("Updated availability of menuItem {} to {}", id, available);
        return saved;
    }

    private void validateActiveRestaurant(UUID restaurantId) {
        RestaurantView restaurant = restaurantClient.getRestaurant(restaurantId);
        if (restaurant == null || !restaurant.isActive()) {
            throw new RestaurantNotActiveException(
                    "Restaurant " + restaurantId + " does not exist or is not ACTIVE");
        }
    }
}
