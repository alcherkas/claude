package com.quickbite.review.repository;

import com.quickbite.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    List<Review> findByDriverIdOrderByCreatedAtDesc(UUID driverId);

    List<Review> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByOrderId(UUID orderId);
}
