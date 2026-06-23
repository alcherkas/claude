package com.quickbite.review.web;

import com.quickbite.review.domain.Review;
import com.quickbite.review.service.ReviewMapper;
import com.quickbite.review.service.ReviewService;
import com.quickbite.review.service.ReviewValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ReviewController.class, InternalReviewController.class})
@Import({ReviewMapper.class, GlobalExceptionHandler.class})
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    void createReturns201WithReviewBody() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Review stored = Review.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .userId(userId)
                .restaurantId(restaurantId)
                .rating(5)
                .comment("Great food")
                .createdAt(Instant.now())
                .build();
        given(reviewService.createReview(eq(userId), any())).willReturn(stored);

        mockMvc.perform(post("/api/reviews")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"" + orderId + "\",\"rating\":5,\"comment\":\"Great food\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great food"));
    }

    @Test
    void listByRestaurantReturnsReviews() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        Review r = Review.builder()
                .id(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .restaurantId(restaurantId)
                .rating(4)
                .comment("Good")
                .createdAt(Instant.now())
                .build();
        given(reviewService.findByRestaurant(restaurantId)).willReturn(List.of(r));

        mockMvc.perform(get("/api/reviews").param("restaurantId", restaurantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("$[0].rating").value(4));
    }

    @Test
    void listWithoutFilterReturns422() throws Exception {
        given(reviewService.findByRestaurant(any())).willThrow(new ReviewValidationException("unused"));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.path").value("/api/reviews"));
    }
}
