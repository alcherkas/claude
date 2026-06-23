package com.quickbite.restaurant.service;

import com.quickbite.restaurant.client.IdentityClient;
import com.quickbite.restaurant.client.UserValidationResponse;
import com.quickbite.restaurant.domain.Restaurant;
import com.quickbite.restaurant.domain.RestaurantStatus;
import com.quickbite.restaurant.dto.CreateRestaurantRequest;
import com.quickbite.restaurant.dto.RestaurantResponse;
import com.quickbite.restaurant.dto.RestaurantSummary;
import com.quickbite.restaurant.event.RestaurantEventProducer;
import com.quickbite.restaurant.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RestaurantService {

    private static final String REQUIRED_ROLE = "RESTAURANT_OWNER";

    private final RestaurantRepository repository;
    private final IdentityClient identityClient;
    private final RestaurantEventProducer eventProducer;

    public RestaurantService(RestaurantRepository repository,
                             IdentityClient identityClient,
                             RestaurantEventProducer eventProducer) {
        this.repository = repository;
        this.identityClient = identityClient;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public RestaurantResponse create(CreateRestaurantRequest request) {
        UserValidationResponse validation = identityClient.validate(request.ownerUserId());
        if (validation == null || !validation.valid()) {
            throw new InvalidOwnerException(request.ownerUserId(), "user does not exist or could not be validated");
        }
        if (!REQUIRED_ROLE.equals(validation.role())) {
            throw new InvalidOwnerException(request.ownerUserId(),
                    "user role is " + validation.role() + " but " + REQUIRED_ROLE + " is required");
        }

        Restaurant restaurant = Restaurant.builder()
                .ownerUserId(request.ownerUserId())
                .name(request.name())
                .cuisine(request.cuisine())
                .addressLine(request.addressLine())
                .city(request.city())
                .lat(request.lat())
                .lng(request.lng())
                .openingHours(request.openingHours())
                .status(RestaurantStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        Restaurant saved = repository.save(restaurant);
        eventProducer.publishUpserted(saved);
        log.info("Created restaurant {} owned by {}", saved.getId(), saved.getOwnerUserId());
        return RestaurantResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> search(String cuisine, String city) {
        String c = StringUtils.hasText(cuisine) ? cuisine : null;
        String ct = StringUtils.hasText(city) ? city : null;
        return repository.search(c, ct).stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantResponse get(UUID id) {
        return RestaurantResponse.from(load(id));
    }

    @Transactional(readOnly = true)
    public RestaurantSummary getSummary(UUID id) {
        return RestaurantSummary.from(load(id));
    }

    @Transactional
    public RestaurantResponse updateStatus(UUID id, RestaurantStatus status) {
        Restaurant restaurant = load(id);
        if (restaurant.getStatus() == status) {
            return RestaurantResponse.from(restaurant);
        }
        restaurant.setStatus(status);
        Restaurant saved = repository.save(restaurant);
        eventProducer.publishStatusChanged(saved);
        log.info("Restaurant {} status changed to {}", saved.getId(), status);
        return RestaurantResponse.from(saved);
    }

    private Restaurant load(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RestaurantNotFoundException(id));
    }
}
