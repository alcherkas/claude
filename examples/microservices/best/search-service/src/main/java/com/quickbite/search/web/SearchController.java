package com.quickbite.search.web;

import com.quickbite.search.domain.SearchDocType;
import com.quickbite.search.dto.SearchResponse;
import com.quickbite.search.service.SearchIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public discovery API, exposed through the gateway at {@code /api/search/**}.
 */
@Tag(name = "Search", description = "Public discovery queries over the denormalized read model.")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchIndexService searchService;

    @Operation(summary = "Search restaurants and menu items by text/cuisine, optionally ranked by proximity")
    @GetMapping
    public SearchResponse search(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cuisine", required = false) String cuisine,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng) {
        return searchService.search(q, cuisine, lat, lng, null);
    }

    @Operation(summary = "Search restaurants only")
    @GetMapping("/restaurants")
    public SearchResponse searchRestaurants(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cuisine", required = false) String cuisine,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng) {
        return searchService.search(q, cuisine, lat, lng, SearchDocType.RESTAURANT);
    }

    @Operation(summary = "Search menu items only")
    @GetMapping("/menu-items")
    public SearchResponse searchMenuItems(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cuisine", required = false) String cuisine,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng) {
        return searchService.search(q, cuisine, lat, lng, SearchDocType.MENU_ITEM);
    }
}
