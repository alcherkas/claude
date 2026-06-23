package com.quickbite.review.web;

import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.service.ReviewMapper;
import com.quickbite.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Internal service-to-service API. Never exposed through the gateway.
 * Lets other services (e.g. restaurant-service) read review data by id or restaurant.
 */
@RestController
@RequestMapping("/internal/reviews")
@RequiredArgsConstructor
public class InternalReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    @GetMapping("/{id}")
    public ReviewResponse getById(@PathVariable UUID id) {
        return reviewMapper.toResponse(reviewService.getReview(id));
    }

    @GetMapping
    public List<ReviewResponse> byRestaurant(@RequestParam UUID restaurantId) {
        return reviewService.findByRestaurant(restaurantId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }
}
