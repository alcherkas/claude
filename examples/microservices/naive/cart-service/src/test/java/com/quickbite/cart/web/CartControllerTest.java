package com.quickbite.cart.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.cart.dto.AddItemRequest;
import com.quickbite.cart.dto.CartItemResponse;
import com.quickbite.cart.dto.CartResponse;
import com.quickbite.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CartService cartService;

    @Test
    void getCartReturnsCart() throws Exception {
        CartResponse response = new CartResponse(
                42L, 7L,
                List.of(new CartItemResponse(1L, 100L, "Margherita", 2, 950, 1900)),
                1900, Instant.parse("2026-06-22T10:00:00Z"));
        when(cartService.getCart(42L)).thenReturn(response);

        mockMvc.perform(get("/api/carts/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.restaurantId").value(7))
                .andExpect(jsonPath("$.subtotalCents").value(1900))
                .andExpect(jsonPath("$.items[0].name").value("Margherita"));
    }

    @Test
    void addItemReturnsCreated() throws Exception {
        CartResponse response = new CartResponse(
                42L, 7L,
                List.of(new CartItemResponse(1L, 100L, "Margherita", 1, 950, 950)),
                950, Instant.parse("2026-06-22T10:05:00Z"));
        when(cartService.addItem(eq(42L), any(AddItemRequest.class))).thenReturn(response);

        AddItemRequest body = new AddItemRequest(100L, 1);

        mockMvc.perform(post("/api/carts/42/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].menuItemId").value(100));
    }

    @Test
    void addItemRejectsInvalidQty() throws Exception {
        AddItemRequest body = new AddItemRequest(100L, 0);

        mockMvc.perform(post("/api/carts/42/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/carts/42/items"));
    }
}
