package com.quickbite.search.web;

import com.quickbite.search.dto.SearchResponse;
import com.quickbite.search.service.SearchIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal service-to-service API under {@code /internal/**}. Never exposed through the
 * gateway (PLATFORM_SPEC §4). Publishes the read model so sibling services can reuse the
 * index without re-querying restaurant/menu directly, and exposes a reconcile hook to
 * repair the index from the authoritative sources when an event was missed.
 */
@Tag(name = "Internal Search", description = "Service-to-service read model access (not gateway-exposed).")
@RestController
@RequestMapping("/internal/search")
@RequiredArgsConstructor
public class InternalSearchController {

    private final SearchIndexService searchService;

    @Operation(summary = "Internal lookup against the search index")
    @GetMapping
    public SearchResponse lookup(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cuisine", required = false) String cuisine) {
        return searchService.search(q, cuisine, null, null, null);
    }

    @Operation(summary = "Reconcile a menu item from its authoritative source (repair a missed event)")
    @PostMapping("/reconcile/menu-items/{id}")
    public ResponseEntity<Void> reconcileMenuItem(@PathVariable("id") UUID id) {
        boolean reconciled = searchService.reconcileMenuItem(id);
        return reconciled ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
