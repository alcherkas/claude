package com.quickbite.pricing.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.pricing.dto.QuoteResponse;
import com.quickbite.pricing.service.PricingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice test for the public quote endpoint.
 */
@WebMvcTest(PricingController.class)
class PricingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PricingService pricingService;

    @Test
    void returnsQuoteForValidRequest() throws Exception {
        QuoteResponse response = new QuoteResponse(
                2000L, 299L, 200L, 160L, 0L, 300L, 2959L, "USD",
                List.of(new QuoteResponse.LineItem(42L, "Margherita", 2, 1000L, 2000L)));
        when(pricingService.quote(any())).thenReturn(response);

        Map<String, Object> body = Map.of(
                "userId", 7,
                "restaurantId", 3,
                "items", List.of(Map.of("menuItemId", 42, "qty", 2)));

        mockMvc.perform(post("/api/pricing/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotalCents").value(2000))
                .andExpect(jsonPath("$.tipCents").value(300))
                .andExpect(jsonPath("$.totalCents").value(2959))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.lineItems[0].name").value("Margherita"));
    }

    @Test
    void rejectsEmptyItems() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", 7,
                "restaurantId", 3,
                "items", List.of());

        mockMvc.perform(post("/api/pricing/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/pricing/quote"));
    }
}
