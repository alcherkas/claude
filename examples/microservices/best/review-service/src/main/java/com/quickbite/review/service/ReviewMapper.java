package com.quickbite.review.service;

import com.quickbite.review.domain.Review;
import com.quickbite.review.dto.ReviewResponse;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrderId(),
                review.getUserId(),
                review.getRestaurantId(),
                review.getDriverId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt());
    }
}
