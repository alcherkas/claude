package com.quickbite.driver.web;

import com.quickbite.driver.dto.CreateDriverRequest;
import com.quickbite.driver.dto.DriverResponse;
import com.quickbite.driver.dto.LocationPingRequest;
import com.quickbite.driver.dto.UpdateAvailabilityRequest;
import com.quickbite.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public courier API exposed through the gateway at {@code /api/drivers/**}.
 */
@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Drivers")
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) {
        this.service = service;
    }

    @Operation(summary = "Register a courier (requires a COURIER identity user)")
    @PostMapping
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody CreateDriverRequest request) {
        DriverResponse response = DriverResponse.from(service.register(request));
        return ResponseEntity.created(URI.create("/api/drivers/" + response.id())).body(response);
    }

    @Operation(summary = "Fetch a driver by id")
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(DriverResponse.from(service.get(id)));
    }

    @Operation(summary = "Update a driver's availability status")
    @PatchMapping("/{id}/availability")
    public ResponseEntity<DriverResponse> updateAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        return ResponseEntity.ok(DriverResponse.from(service.updateAvailability(id, request.status())));
    }

    @Operation(summary = "Record a driver location ping")
    @PostMapping("/{id}/location")
    public ResponseEntity<DriverResponse> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody LocationPingRequest request) {
        return ResponseEntity.ok(DriverResponse.from(service.updateLocation(id, request.lat(), request.lng())));
    }

    @Operation(summary = "List available drivers nearest to a location")
    @GetMapping("/available")
    public ResponseEntity<List<DriverResponse>> available(
            @RequestParam double lat,
            @RequestParam double lng) {
        List<DriverResponse> drivers = service.findAvailable(lat, lng, 20).stream()
                .map(DriverResponse::from)
                .toList();
        return ResponseEntity.ok(drivers);
    }
}
