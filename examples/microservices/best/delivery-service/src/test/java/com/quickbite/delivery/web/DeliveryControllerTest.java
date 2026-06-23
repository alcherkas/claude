package com.quickbite.delivery.web;

import com.quickbite.delivery.domain.Delivery;
import com.quickbite.delivery.domain.DeliveryStatus;
import com.quickbite.delivery.service.DeliveryMapper;
import com.quickbite.delivery.service.DeliveryNotFoundException;
import com.quickbite.delivery.service.DeliveryService;
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

@WebMvcTest(controllers = {DeliveryController.class, InternalDeliveryController.class})
@Import({DeliveryMapper.class, GlobalExceptionHandler.class})
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @Test
    void getDeliveryReturnsDelivery() throws Exception {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        Delivery delivery = Delivery.builder()
                .id(id)
                .orderId(orderId)
                .driverId(driverId)
                .status(DeliveryStatus.ASSIGNED)
                .createdAt(Instant.now())
                .build();
        given(deliveryService.getDelivery(id)).willReturn(delivery);

        mockMvc.perform(get("/api/deliveries/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void getMissingDeliveryReturns404WithErrorBody() throws Exception {
        UUID id = UUID.randomUUID();
        given(deliveryService.getDelivery(id))
                .willThrow(new DeliveryNotFoundException("Delivery " + id + " not found"));

        mockMvc.perform(get("/api/deliveries/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/deliveries/" + id));
    }
}
