package com.quickbite.driver.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quickbite.driver.domain.Driver;
import com.quickbite.driver.domain.DriverStatus;
import com.quickbite.driver.domain.Vehicle;
import com.quickbite.driver.service.DriverNotFoundException;
import com.quickbite.driver.service.DriverService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {DriverController.class, InternalDriverController.class})
class DriverControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DriverService service;

    private Driver sampleDriver(UUID id, UUID userId, DriverStatus status) {
        return Driver.builder()
                .id(id)
                .userId(userId)
                .name("Alex Courier")
                .vehicle(Vehicle.BIKE)
                .status(status)
                .lat(52.52)
                .lng(13.40)
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(service.register(any())).thenReturn(sampleDriver(id, userId, DriverStatus.OFFLINE));

        String body = """
                {
                  "userId": "%s",
                  "name": "Alex Courier",
                  "vehicle": "BIKE"
                }
                """.formatted(userId);

        mockMvc.perform(post("/api/drivers")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("OFFLINE"));
    }

    @Test
    void createWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/drivers")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/drivers"));
    }

    @Test
    void getMissingReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenThrow(new DriverNotFoundException(id));
        mockMvc.perform(get("/api/drivers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void assignReturnsOnDeliveryStatus() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.assign(id)).thenReturn(sampleDriver(id, UUID.randomUUID(), DriverStatus.ON_DELIVERY));
        mockMvc.perform(post("/internal/drivers/{id}/assign", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_DELIVERY"));
    }
}
