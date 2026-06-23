package com.quickbite.driver.service;

import com.quickbite.driver.client.IdentityClient;
import com.quickbite.driver.client.UserValidationResponse;
import com.quickbite.driver.domain.Driver;
import com.quickbite.driver.domain.DriverStatus;
import com.quickbite.driver.dto.CreateDriverRequest;
import com.quickbite.driver.repository.DriverRepository;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for couriers: registration (gated on the COURIER role in
 * identity-service), availability transitions, location pings, and nearest-driver
 * lookup used by the public API and by delivery-service's dispatch flow.
 */
@Slf4j
@Service
public class DriverService {

    /** Role required in identity-service for a user to operate as a driver. */
    private static final String COURIER_ROLE = "COURIER";

    private final DriverRepository repository;
    private final IdentityClient identityClient;

    public DriverService(DriverRepository repository, IdentityClient identityClient) {
        this.repository = repository;
        this.identityClient = identityClient;
    }

    @Transactional
    public Driver register(CreateDriverRequest request) {
        UserValidationResponse validation = identityClient.validate(request.userId(), COURIER_ROLE);
        if (validation == null || !validation.valid()) {
            throw new InvalidCourierException(request.userId());
        }
        if (repository.existsByUserId(request.userId())) {
            throw new DriverConflictException("User " + request.userId() + " already has a driver profile");
        }

        Driver driver = Driver.builder()
                .userId(request.userId())
                .name(request.name())
                .vehicle(request.vehicle())
                .status(DriverStatus.OFFLINE)
                .build();
        Driver saved = repository.save(driver);
        log.info("Registered driver {} for courier user {}", saved.getId(), saved.getUserId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Driver get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new DriverNotFoundException(id));
    }

    @Transactional
    public Driver updateAvailability(UUID id, DriverStatus status) {
        Driver driver = get(id);
        driver.setStatus(status);
        log.info("Driver {} availability set to {}", id, status);
        return repository.save(driver);
    }

    @Transactional
    public Driver updateLocation(UUID id, double lat, double lng) {
        Driver driver = get(id);
        driver.setLat(lat);
        driver.setLng(lng);
        return repository.save(driver);
    }

    /**
     * Returns up to {@code limit} AVAILABLE drivers, nearest to (lat,lng) first.
     */
    @Transactional(readOnly = true)
    public List<Driver> findAvailable(double lat, double lng, int limit) {
        return repository.findNearest(DriverStatus.AVAILABLE, lat, lng, PageRequest.of(0, limit));
    }

    /**
     * Picks the single nearest AVAILABLE driver to (lat,lng), if any. Used by
     * delivery-service to choose a courier before assignment.
     */
    @Transactional(readOnly = true)
    public Driver findNearestAvailable(double lat, double lng) {
        return findAvailable(lat, lng, 1).stream()
                .findFirst()
                .orElseThrow(() -> new DriverNotFoundException(new UUID(0L, 0L)));
    }

    /**
     * Transitions an AVAILABLE driver to ON_DELIVERY. Idempotency is intentionally
     * strict: a driver already ON_DELIVERY (or OFFLINE) cannot be assigned again.
     */
    @Transactional
    public Driver assign(UUID id) {
        Driver driver = get(id);
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new DriverConflictException(
                    "Driver " + id + " is not AVAILABLE (current status: " + driver.getStatus() + ")");
        }
        driver.setStatus(DriverStatus.ON_DELIVERY);
        log.info("Driver {} assigned to a delivery (-> ON_DELIVERY)", id);
        return repository.save(driver);
    }
}
