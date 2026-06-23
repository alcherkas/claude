package com.quickbite.order.client;

import com.quickbite.order.dto.MenuItemSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "menuClient", url = "${clients.menu.url}", fallback = MenuClientFallback.class)
public interface MenuClient {

    @GetMapping("/internal/menu-items/{id}")
    MenuItemSummary getMenuItem(@PathVariable("id") UUID id);
}
