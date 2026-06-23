package com.quickbite.order.web;

import com.quickbite.order.domain.Order;
import com.quickbite.order.domain.OrderStatus;
import com.quickbite.order.service.OrderMapper;
import com.quickbite.order.service.OrderNotFoundException;
import com.quickbite.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OrderController.class, InternalOrderController.class})
@Import({OrderMapper.class, GlobalExceptionHandler.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrderReturnsOrder() throws Exception {
        UUID id = UUID.randomUUID();
        Order order = Order.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .restaurantId(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .subtotalCents(1000)
                .deliveryFeeCents(200)
                .serviceFeeCents(100)
                .taxCents(80)
                .discountCents(0)
                .totalCents(1380)
                .currency("USD")
                .createdAt(Instant.now())
                .build();
        given(orderService.getOrder(id)).willReturn(order);

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalCents").value(1380));
    }

    @Test
    void getMissingOrderReturns404WithErrorBody() throws Exception {
        UUID id = UUID.randomUUID();
        given(orderService.getOrder(id)).willThrow(new OrderNotFoundException("Order " + id + " not found"));

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/orders/" + id));
    }
}
