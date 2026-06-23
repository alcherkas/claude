package com.quickbite.menu.web;

import com.quickbite.menu.dto.CreateMenuItemRequest;
import com.quickbite.menu.dto.MenuItemResponse;
import com.quickbite.menu.dto.UpdateAvailabilityRequest;
import com.quickbite.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Public menu API. Reached externally through the gateway at /api/menu/**.
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    public ResponseEntity<MenuItemResponse> create(@Valid @RequestBody CreateMenuItemRequest request) {
        MenuItemResponse body = MenuItemResponse.from(menuService.create(request));
        return ResponseEntity.created(URI.create("/api/menu/" + body.id())).body(body);
    }

    @GetMapping
    public List<MenuItemResponse> listByRestaurant(@RequestParam("restaurantId") UUID restaurantId) {
        return menuService.listByRestaurant(restaurantId).stream()
                .map(MenuItemResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public MenuItemResponse get(@PathVariable("id") UUID id) {
        return MenuItemResponse.from(menuService.get(id));
    }

    @PatchMapping("/{id}/availability")
    @ResponseStatus(HttpStatus.OK)
    public MenuItemResponse updateAvailability(@PathVariable("id") UUID id,
                                               @Valid @RequestBody UpdateAvailabilityRequest request) {
        return MenuItemResponse.from(menuService.updateAvailability(id, request.available()));
    }
}
