package com.quickbite.cart.client;

import com.quickbite.cart.dto.MenuItemView;
import com.quickbite.cart.web.NotFoundException;
import com.quickbite.cart.web.UpstreamUnavailableException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback for {@link MenuClient}. A genuine 404 from menu-service
 * is surfaced as a NotFound; any other failure (circuit open, timeout, 5xx)
 * is surfaced as upstream-unavailable so the cart operation fails closed.
 */
@Slf4j
@Component
public class MenuClientFallbackFactory implements FallbackFactory<MenuClient> {

    @Override
    public MenuClient create(Throwable cause) {
        return new MenuClient() {
            @Override
            public MenuItemView getMenuItem(Long id) {
                if (cause instanceof FeignException.NotFound) {
                    throw new NotFoundException("Menu item " + id + " does not exist");
                }
                log.warn("menu-service unavailable while fetching menu item {}: {}", id, cause.toString());
                throw new UpstreamUnavailableException("menu-service is unavailable; cannot re-price item " + id);
            }
        };
    }
}
