package com.quickbite.order.client;

import com.quickbite.order.dto.MenuItemSummary;
import com.quickbite.order.service.DependencyUnavailableException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MenuClientFallback implements MenuClient {

    @Override
    public MenuItemSummary getMenuItem(UUID id) {
        throw new DependencyUnavailableException("menu-service is unavailable; cannot validate menu item");
    }
}
