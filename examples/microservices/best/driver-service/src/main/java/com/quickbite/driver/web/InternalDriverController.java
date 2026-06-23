package com.quickbite.driver.web;

import com.quickbite.driver.dto.InternalDriverResponse;
import com.quickbite.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints under {@code /internal/**}. Never exposed through
 * the gateway; called by delivery-service to find and assign a courier.
 */
@RestController
@RequestMapping("/internal/drivers")
@Tag(name = "Internal")
public class InternalDriverController {

    private final DriverService service;

    public InternalDriverController(DriverService service) {
        this.service = service;
    }

    @Operation(summary = "Find the nearest AVAILABLE driver to a pickup location (internal)")
    @GetMapping("/available")
    public ResponseEntity<InternalDriverResponse> nearestAvailable(
            @RequestParam double lat,
            @RequestParam double lng) {
        return ResponseEntity.ok(InternalDriverResponse.from(service.findNearestAvailable(lat, lng)));
    }

    @Operation(summary = "Assign a driver to a delivery, transitioning to ON_DELIVERY (internal)")
    @PostMapping("/{id}/assign")
    public ResponseEntity<InternalDriverResponse> assign(@PathVariable UUID id) {
        return ResponseEntity.ok(InternalDriverResponse.from(service.assign(id)));
    }
}
