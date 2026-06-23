package com.quickbite.review.service;

import com.quickbite.review.client.IdentityClient;
import com.quickbite.review.client.OrderClient;
import com.quickbite.review.client.RestaurantClient;
import com.quickbite.review.domain.Review;
import com.quickbite.review.dto.CreateReviewRequest;
import com.quickbite.review.dto.OrderSummary;
import com.quickbite.review.dto.RestaurantSummary;
import com.quickbite.review.dto.UserSummary;
import com.quickbite.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderClient orderClient;
    private final RestaurantClient restaurantClient;
    private final IdentityClient identityClient;

    /**
     * Creates a review after verifying — via order-service /internal — that the order is
     * DELIVERED and belongs to the requesting user. The restaurant (and optional driver) are
     * derived from the authoritative order summary, not trusted from the caller.
     */
    @Transactional
    public Review createReview(UUID userId, CreateReviewRequest request) {
        // Confirm the reviewing user is a real, active account (identity /internal).
        UserSummary user = identityClient.getUser(userId);
        if (user == null || !user.active()) {
            throw new ReviewValidationException("User " + userId + " is not a valid active account");
        }

        OrderSummary order = orderClient.getOrder(request.orderId());
        if (order == null) {
            throw new ReviewValidationException("Order " + request.orderId() + " could not be found");
        }
        if (!order.userId().equals(userId)) {
            throw new ReviewValidationException("Order " + request.orderId() + " does not belong to this user");
        }
        if (!order.isDelivered()) {
            throw new ReviewValidationException(
                    "Order " + request.orderId() + " is not DELIVERED (status=" + order.status() + ")");
        }
        if (reviewRepository.existsByOrderId(request.orderId())) {
            throw new ReviewValidationException("Order " + request.orderId() + " has already been reviewed");
        }

        // Optional enrichment: confirm the restaurant resolves. Degrades gracefully if unavailable.
        RestaurantSummary restaurant = restaurantClient.getRestaurant(order.restaurantId());
        if (restaurant != null) {
            log.debug("Reviewing restaurant {} ({})", restaurant.name(), restaurant.status());
        }

        Review review = Review.builder()
                .id(UUID.randomUUID())
                .orderId(order.id())
                .userId(userId)
                .restaurantId(order.restaurantId())
                .driverId(null)
                .rating(request.rating())
                .comment(request.comment())
                .createdAt(Instant.now())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Stored review {} for order {} by user {}", saved.getId(), saved.getOrderId(), userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Review getReview(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Review> findByRestaurant(UUID restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
    }

    @Transactional(readOnly = true)
    public List<Review> findByDriver(UUID driverId) {
        return reviewRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
    }

    @Transactional(readOnly = true)
    public List<Review> findByUser(UUID userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
