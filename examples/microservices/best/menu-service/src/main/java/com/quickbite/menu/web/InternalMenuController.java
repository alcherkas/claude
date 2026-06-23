package com.quickbite.menu.web;

import com.quickbite.menu.dto.InternalMenuItemResponse;
import com.quickbite.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Service-to-service API. Never exposed through the gateway. Consumed by
 * cart-service, pricing-service and order-service to fetch authoritative prices.
 */
@RestController
@RequestMapping("/internal/menu-items")
@RequiredArgsConstructor
public class InternalMenuController {

    private final MenuService menuService;

    @GetMapping("/{id}")
    public InternalMenuItemResponse get(@PathVariable("id") UUID id) {
        return InternalMenuItemResponse.from(menuService.get(id));
    }
}
