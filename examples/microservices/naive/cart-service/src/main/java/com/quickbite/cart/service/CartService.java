package com.quickbite.cart.service;

import com.quickbite.cart.client.IdentityClient;
import com.quickbite.cart.client.MenuClient;
import com.quickbite.cart.domain.Cart;
import com.quickbite.cart.domain.CartItem;
import com.quickbite.cart.dto.AddItemRequest;
import com.quickbite.cart.dto.CartItemResponse;
import com.quickbite.cart.dto.CartResponse;
import com.quickbite.cart.dto.CartSnapshot;
import com.quickbite.cart.dto.MenuItemView;
import com.quickbite.cart.dto.UpdateItemRequest;
import com.quickbite.cart.dto.UserValidation;
import com.quickbite.cart.repository.CartRepository;
import com.quickbite.cart.web.CartConflictException;
import com.quickbite.cart.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MenuClient menuClient;
    private final IdentityClient identityClient;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findById(userId).orElseGet(() -> new Cart(userId));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Long userId, AddItemRequest request) {
        // Validate the owning user against identity-service before building a cart.
        UserValidation user = identityClient.validateUser(userId);
        if (!user.valid()) {
            throw new NotFoundException("User " + userId + " is not a valid active user");
        }

        Cart cart = cartRepository.findById(userId).orElseGet(() -> new Cart(userId));

        MenuItemView menuItem = menuClient.getMenuItem(request.menuItemId());
        if (!menuItem.available()) {
            throw new CartConflictException("Menu item " + menuItem.id() + " is not available");
        }

        // A cart is restaurant-scoped: reject items from a different restaurant.
        if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(menuItem.restaurantId())) {
            throw new CartConflictException(
                    "Cart already contains items from restaurant " + cart.getRestaurantId()
                            + "; cannot add an item from restaurant " + menuItem.restaurantId());
        }
        if (cart.getRestaurantId() == null) {
            cart.setRestaurantId(menuItem.restaurantId());
        }

        // Merge with an existing line for the same menu item, otherwise add a new line.
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getMenuItemId().equals(menuItem.id()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQty(existing.getQty() + request.qty());
            existing.setUnitPriceCents(menuItem.priceCents());
            existing.setName(menuItem.name());
        } else {
            cart.addItem(new CartItem(menuItem.id(), menuItem.name(), request.qty(), menuItem.priceCents()));
        }

        cart.touch();
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateItemRequest request) {
        Cart cart = requireCart(userId);
        CartItem item = requireItem(cart, itemId);
        item.setQty(request.qty());
        cart.touch();
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = requireCart(userId);
        CartItem item = requireItem(cart, itemId);
        cart.removeItem(item);
        if (cart.isEmpty()) {
            cart.setRestaurantId(null);
        }
        cart.touch();
        return toResponse(cartRepository.save(cart));
    }

    /**
     * Produce an immutable checkout snapshot. The cart must be non-empty.
     * The snapshot is also exposed to order-service via the /internal endpoint.
     */
    @Transactional(readOnly = true)
    public CartSnapshot checkout(Long userId) {
        Cart cart = requireCart(userId);
        if (cart.isEmpty()) {
            throw new CartConflictException("Cart for user " + userId + " is empty; nothing to check out");
        }
        return toSnapshot(cart);
    }

    @Transactional(readOnly = true)
    public CartSnapshot snapshot(Long userId) {
        Cart cart = requireCart(userId);
        return toSnapshot(cart);
    }

    private Cart requireCart(Long userId) {
        return cartRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No cart for user " + userId));
    }

    private CartItem requireItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item " + itemId + " not found in cart for user " + cart.getUserId()));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(i.getId(), i.getMenuItemId(), i.getName(),
                        i.getQty(), i.getUnitPriceCents(), i.lineTotalCents()))
                .toList();
        return new CartResponse(cart.getUserId(), cart.getRestaurantId(), items,
                cart.subtotalCents(), cart.getUpdatedAt());
    }

    private CartSnapshot toSnapshot(Cart cart) {
        List<CartSnapshot.SnapshotItem> items = cart.getItems().stream()
                .map(i -> new CartSnapshot.SnapshotItem(i.getMenuItemId(), i.getName(), i.getQty(), i.getUnitPriceCents()))
                .toList();
        return new CartSnapshot(cart.getUserId(), cart.getRestaurantId(), items, cart.subtotalCents());
    }
}
