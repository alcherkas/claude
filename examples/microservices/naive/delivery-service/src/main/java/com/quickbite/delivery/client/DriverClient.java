package com.quickbite.delivery.client;

import com.quickbite.delivery.dto.DriverSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(name = "driverClient", url = "${clients.driver.url}", fallback = DriverClientFallback.class)
public interface DriverClient {

    /** Returns an AVAILABLE driver to dispatch, or null when none are free. */
    @GetMapping("/internal/drivers/available")
    DriverSummary findAvailable();

    /** Marks the driver ON_DELIVERY and returns the updated record. */
    @PostMapping("/internal/drivers/{id}/assign")
    DriverSummary assign(@PathVariable("id") UUID id);
}
