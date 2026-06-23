package com.quickbite.menu.web;

import com.quickbite.menu.domain.MenuItem;
import com.quickbite.menu.service.MenuService;
import com.quickbite.menu.service.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice test for the public menu controller, including the global error handler.
 */
@WebMvcTest(controllers = {MenuController.class, InternalMenuController.class})
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @Test
    void createReturns201WithBody() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        MenuItem saved = MenuItem.builder()
                .id(UUID.randomUUID())
                .restaurantId(restaurantId)
                .name("Margherita")
                .description("Tomato, mozzarella, basil")
                .priceCents(1199)
                .currency("EUR")
                .category("Pizza")
                .available(true)
                .createdAt(Instant.now())
                .build();
        when(menuService.create(any())).thenReturn(saved);

        String body = """
                {
                  "restaurantId": "%s",
                  "name": "Margherita",
                  "description": "Tomato, mozzarella, basil",
                  "priceCents": 1199,
                  "currency": "EUR",
                  "category": "Pizza",
                  "available": true
                }
                """.formatted(restaurantId);

        mockMvc.perform(post("/api/menu")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Margherita"))
                .andExpect(jsonPath("$.priceCents").value(1199))
                .andExpect(jsonPath("$.restaurantId").value(restaurantId.toString()));
    }

    @Test
    void createRejectsInvalidCurrency() throws Exception {
        String body = """
                {
                  "restaurantId": "%s",
                  "name": "Margherita",
                  "priceCents": 1199,
                  "currency": "euro",
                  "category": "Pizza",
                  "available": true
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/menu")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/menu"));
    }

    @Test
    void getUnknownReturns404Envelope() throws Exception {
        UUID id = UUID.randomUUID();
        when(menuService.get(id)).thenThrow(new NotFoundException("Menu item not found: " + id));

        mockMvc.perform(get("/api/menu/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/menu/" + id));
    }
}
