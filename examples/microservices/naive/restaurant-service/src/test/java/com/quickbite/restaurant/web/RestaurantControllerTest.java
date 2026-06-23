package com.quickbite.restaurant.web;

import com.quickbite.restaurant.domain.RestaurantStatus;
import com.quickbite.restaurant.dto.RestaurantResponse;
import com.quickbite.restaurant.service.RestaurantNotFoundException;
import com.quickbite.restaurant.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RestaurantController.class, InternalRestaurantController.class})
class RestaurantControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RestaurantService service;

    @Test
    void createReturns201WithLocation() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        RestaurantResponse response = new RestaurantResponse(
                id, owner, "Pizza Place", "Italian", "1 Main St", "Berlin",
                52.52, 13.40, RestaurantStatus.PENDING, "Mon-Sun 11-23", Instant.now());
        when(service.create(any())).thenReturn(response);

        String body = """
                {
                  "ownerUserId": "%s",
                  "name": "Pizza Place",
                  "cuisine": "Italian",
                  "addressLine": "1 Main St",
                  "city": "Berlin",
                  "lat": 52.52,
                  "lng": 13.40,
                  "openingHours": "Mon-Sun 11-23"
                }
                """.formatted(owner);

        mockMvc.perform(post("/api/restaurants")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/restaurants")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/restaurants"));
    }

    @Test
    void listReturnsArray() throws Exception {
        when(service.search(null, "Berlin")).thenReturn(List.of());
        mockMvc.perform(get("/api/restaurants").param("city", "Berlin"))
                .andExpect(status().isOk());
    }

    @Test
    void getMissingReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenThrow(new RestaurantNotFoundException(id));
        mockMvc.perform(get("/api/restaurants/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
