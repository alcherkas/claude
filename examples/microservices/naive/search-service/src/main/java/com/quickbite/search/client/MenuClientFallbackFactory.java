package com.quickbite.search.client;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resilience4j fallback for {@link MenuClient}. Backfill is best-effort: on a 404 or any
 * upstream failure (circuit open, timeout, 5xx) we return {@code null} so the caller can
 * proceed without the enrichment instead of failing the event-processing flow.
 */
@Slf4j
@Component
public class MenuClientFallbackFactory implements FallbackFactory<MenuClient> {

    @Override
    public MenuClient create(Throwable cause) {
        return new MenuClient() {
            @Override
            public MenuItemView getMenuItem(UUID id) {
                if (cause instanceof FeignException.NotFound) {
                    log.debug("menu-service has no menu item {} for index backfill", id);
                } else {
                    log.warn("menu-service unavailable while backfilling menu item {}: {}",
                            id, cause.toString());
                }
                return null;
            }
        };
    }
}
