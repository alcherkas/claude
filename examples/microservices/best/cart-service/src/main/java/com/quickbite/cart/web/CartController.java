package com.quickbite.cart.web;

import com.quickbite.cart.dto.AddItemRequest;
import com.quickbite.cart.dto.CartResponse;
import com.quickbite.cart.dto.CartSnapshot;
import com.quickbite.cart.dto.UpdateItemRequest;
import com.quickbite.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public cart API, reached through the gateway at /api/carts/**. */
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public CartResponse getCart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long userId,
                                                @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(userId, request));
    }

    @PutMapping("/{userId}/items/{itemId}")
    public CartResponse updateItem(@PathVariable Long userId,
                                   @PathVariable Long itemId,
                                   @Valid @RequestBody UpdateItemRequest request) {
        return cartService.updateItem(userId, itemId, request);
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public CartResponse removeItem(@PathVariable Long userId, @PathVariable Long itemId) {
        return cartService.removeItem(userId, itemId);
    }

    @PostMapping("/{userId}/checkout")
    public CartSnapshot checkout(@PathVariable Long userId) {
        return cartService.checkout(userId);
    }
}
