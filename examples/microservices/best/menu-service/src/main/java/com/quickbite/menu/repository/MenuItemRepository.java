package com.quickbite.menu.repository;

import com.quickbite.menu.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByRestaurantIdOrderByCategoryAscNameAsc(UUID restaurantId);
}
