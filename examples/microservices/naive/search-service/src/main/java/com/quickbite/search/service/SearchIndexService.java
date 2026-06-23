package com.quickbite.search.service;

import com.quickbite.search.client.MenuClient;
import com.quickbite.search.client.MenuItemView;
import com.quickbite.search.client.RestaurantClient;
import com.quickbite.search.client.RestaurantView;
import com.quickbite.search.domain.SearchDoc;
import com.quickbite.search.domain.SearchDocType;
import com.quickbite.search.dto.SearchResponse;
import com.quickbite.search.dto.SearchResultDto;
import com.quickbite.search.event.payload.MenuItemPayload;
import com.quickbite.search.event.payload.RestaurantPayload;
import com.quickbite.search.repository.SearchDocRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Owns the denormalized read model: applies upserts driven by Kafka events and answers
 * the public search queries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexService {

    private static final int MAX_RESULTS = 50;
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final SearchDocRepository repository;
    private final RestaurantClient restaurantClient;
    private final MenuClient menuClient;

    /* ----------------------------- indexing (event side) ----------------------------- */

    @Transactional
    public void upsertRestaurant(RestaurantPayload p) {
        if (p.id() == null) {
            log.warn("Skipping restaurant upsert with null id");
            return;
        }
        SearchDoc doc = repository.findByTypeAndRefId(SearchDocType.RESTAURANT, p.id())
                .orElseGet(() -> SearchDoc.builder()
                        .id(SearchDoc.deterministicId(SearchDocType.RESTAURANT, p.id()))
                        .type(SearchDocType.RESTAURANT)
                        .refId(p.id())
                        .build());

        doc.setRestaurantId(p.id());
        doc.setName(p.name());
        doc.setCuisine(p.cuisine());
        doc.setPriceCents(null);
        doc.setLat(p.lat());
        doc.setLng(p.lng());
        doc.setAvailable(p.isAvailable());
        doc.setUpdatedAt(Instant.now());

        repository.save(doc);
        log.debug("Indexed restaurant {} (available={})", p.id(), p.isAvailable());
    }

    @Transactional
    public void upsertMenuItem(MenuItemPayload p) {
        if (p.id() == null || p.restaurantId() == null) {
            log.warn("Skipping menu item upsert with null id/restaurantId");
            return;
        }
        SearchDoc doc = repository.findByTypeAndRefId(SearchDocType.MENU_ITEM, p.id())
                .orElseGet(() -> SearchDoc.builder()
                        .id(SearchDoc.deterministicId(SearchDocType.MENU_ITEM, p.id()))
                        .type(SearchDocType.MENU_ITEM)
                        .refId(p.id())
                        .build());

        // Inherit geo/cuisine from the parent restaurant doc when present, so menu-item
        // hits can still be ranked/filtered by location and cuisine. If the parent doc is
        // missing (e.g. the menu event arrived before its restaurant event), backfill it
        // synchronously from restaurant-service's /internal endpoint — best-effort: the
        // Feign fallback returns null when the dependency is unavailable.
        repository.findByTypeAndRefId(SearchDocType.RESTAURANT, p.restaurantId())
                .ifPresentOrElse(parent -> {
                    doc.setCuisine(parent.getCuisine());
                    doc.setLat(parent.getLat());
                    doc.setLng(parent.getLng());
                }, () -> {
                    RestaurantView parent = restaurantClient.getRestaurant(p.restaurantId());
                    if (parent != null) {
                        doc.setCuisine(parent.cuisine());
                        doc.setLat(parent.lat());
                        doc.setLng(parent.lng());
                        upsertRestaurant(new RestaurantPayload(
                                parent.id(), parent.name(), parent.cuisine(),
                                parent.lat(), parent.lng(), parent.status()));
                    }
                });

        doc.setRestaurantId(p.restaurantId());
        doc.setName(p.name());
        doc.setPriceCents(p.priceCents());
        doc.setAvailable(p.available());
        doc.setUpdatedAt(Instant.now());

        repository.save(doc);
        log.debug("Indexed menu item {} for restaurant {}", p.id(), p.restaurantId());
    }

    /**
     * On-demand reconciliation of a single menu item against the authoritative source.
     * Pulls the item from menu-service {@code /internal}, ensures its parent restaurant is
     * indexed (via restaurant-service {@code /internal}), then upserts the menu-item doc.
     * Used to repair the index when an event was missed. Returns false if the source no
     * longer has the item or the dependency is unavailable (Feign fallback returns null).
     */
    @Transactional
    public boolean reconcileMenuItem(UUID menuItemId) {
        MenuItemView item = menuClient.getMenuItem(menuItemId);
        if (item == null) {
            log.info("Cannot reconcile menu item {} — not returned by menu-service", menuItemId);
            return false;
        }
        RestaurantView parent = restaurantClient.getRestaurant(item.restaurantId());
        if (parent != null) {
            upsertRestaurant(new RestaurantPayload(
                    parent.id(), parent.name(), parent.cuisine(),
                    parent.lat(), parent.lng(), parent.status()));
        }
        upsertMenuItem(new MenuItemPayload(
                item.id(), item.restaurantId(), item.name(),
                null, item.priceCents(), item.available()));
        return true;
    }

    /* ----------------------------- querying (web side) ----------------------------- */

    @Transactional(readOnly = true)
    public SearchResponse search(String q, String cuisine, Double lat, Double lng, SearchDocType type) {
        String normalizedQ = StringUtils.hasText(q) ? q.trim() : null;
        String normalizedCuisine = StringUtils.hasText(cuisine) ? cuisine.trim() : null;

        Pageable pageable = PageRequest.of(0, MAX_RESULTS, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<SearchDoc> page = repository.search(normalizedQ, normalizedCuisine, type, pageable);

        List<SearchResultDto> results = page.getContent().stream()
                .map(d -> SearchResultDto.from(d, distanceKm(lat, lng, d.getLat(), d.getLng())))
                .sorted(Comparator.comparing(
                        SearchResultDto::distanceKm,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return new SearchResponse(normalizedQ, normalizedCuisine, results.size(), results);
    }

    /** Great-circle (haversine) distance in km, or null when either point is unknown. */
    private Double distanceKm(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return null;
        }
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
