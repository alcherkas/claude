package com.quickbite.pricing.client;

import com.quickbite.pricing.dto.MenuItemView;
import com.quickbite.pricing.exception.UpstreamUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback for {@link MenuClient}. Menu prices are authoritative and
 * cannot be guessed, so a failure surfaces as a 503 rather than a silent zero.
 */
@Slf4j
@Component
public class MenuClientFallbackFactory implements FallbackFactory<MenuClient> {

    @Override
    public MenuClient create(Throwable cause) {
        log.warn("menu-service unavailable, failing pricing quote: {}", cause.toString());
        return new MenuClient() {
            @Override
            public MenuItemView getMenuItem(Long id) {
                throw new UpstreamUnavailableException(
                        "menu-service unavailable while pricing menu item " + id, cause);
            }
        };
    }
}
