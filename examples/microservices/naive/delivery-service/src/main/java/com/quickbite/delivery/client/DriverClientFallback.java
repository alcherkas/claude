package com.quickbite.delivery.client;

import com.quickbite.delivery.dto.DriverSummary;
import com.quickbite.delivery.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DriverClientFallback implements DriverClient {

    @Override
    public DriverSummary findAvailable() {
        throw new DependencyUnavailableException("driver-service is unavailable; cannot find an available driver");
    }

    @Override
    public DriverSummary assign(UUID id) {
        throw new DependencyUnavailableException("driver-service is unavailable; cannot assign driver " + id);
    }
}
