package com.quickbite.review.web;

import com.quickbite.review.dto.CreateReviewRequest;
import com.quickbite.review.dto.ReviewResponse;
import com.quickbite.review.service.ReviewMapper;
import com.quickbite.review.service.ReviewService;
import com.quickbite.review.service.ReviewValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Public review API, exposed through the gateway at /api/reviews/**.
 * The authenticated user id arrives in the X-User-Id header the gateway injects after JWT validation.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@RequestHeader("X-User-Id") UUID userId,
                                 @Valid @RequestBody CreateReviewRequest request) {
        return reviewMapper.toResponse(reviewService.createReview(userId, request));
    }

    /**
     * Lists reviews filtered by exactly one of restaurantId / driverId / userId.
     * Examples: GET /api/reviews?restaurantId=..., ?driverId=..., ?userId=...
     */
    @GetMapping
    public List<ReviewResponse> list(@RequestParam(required = false) UUID restaurantId,
                                     @RequestParam(required = false) UUID driverId,
                                     @RequestParam(required = false) UUID userId) {
        long provided = java.util.stream.Stream.of(restaurantId, driverId, userId)
                .filter(java.util.Objects::nonNull)
                .count();
        if (provided != 1) {
            throw new ReviewValidationException(
                    "Provide exactly one of restaurantId, driverId or userId");
        }

        List<com.quickbite.review.domain.Review> reviews;
        if (restaurantId != null) {
            reviews = reviewService.findByRestaurant(restaurantId);
        } else if (driverId != null) {
            reviews = reviewService.findByDriver(driverId);
        } else {
            reviews = reviewService.findByUser(userId);
        }
        return reviews.stream().map(reviewMapper::toResponse).toList();
    }
}
