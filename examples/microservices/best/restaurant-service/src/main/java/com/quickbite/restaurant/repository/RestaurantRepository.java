package com.quickbite.restaurant.repository;

import com.quickbite.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    @Query("""
            SELECT r FROM Restaurant r
            WHERE (:cuisine IS NULL OR LOWER(r.cuisine) = LOWER(:cuisine))
              AND (:city IS NULL OR LOWER(r.city) = LOWER(:city))
            ORDER BY r.createdAt DESC
            """)
    List<Restaurant> search(@Param("cuisine") String cuisine, @Param("city") String city);
}
