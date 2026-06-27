package com.quickbite.order.service;

import com.quickbite.order.client.CartClient;
import com.quickbite.order.client.IdentityClient;
import com.quickbite.order.client.PricingClient;
import com.quickbite.order.client.RestaurantClient;
import com.quickbite.order.domain.Order;
import com.quickbite.order.domain.OrderItem;
import com.quickbite.order.domain.OrderStatus;
import com.quickbite.order.dto.CartSnapshot;
import com.quickbite.order.dto.CreateOrderRequest;
import com.quickbite.order.dto.PricingQuoteRequest;
import com.quickbite.order.dto.PricingQuoteResponse;
import com.quickbite.order.dto.RestaurantSummary;
import com.quickbite.order.dto.UserSummary;
import com.quickbite.order.event.OrderEventProducer;
import com.quickbite.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final PricingClient pricingClient;
    private final RestaurantClient restaurantClient;
    private final IdentityClient identityClient;
    private final OrderEventProducer eventProducer;

    @Value("${order.currency:USD}")
    private String defaultCurrency;

    /**
     * Creation flow:
     * 1) load cart snapshot from cart-service
     * 2) validate user (identity) and restaurant ACTIVE (restaurant)
     * 3) get authoritative pricing quote (pricing)
     * 4) persist order (CREATED) + items + pricing snapshot
     * 5) emit OrderCreated
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        UUID userId = request.userId();

        UserSummary user = identityClient.getUser(userId);
        if (user == null) {
            throw new OrderValidationException("Unknown user " + userId);
        }

        CartSnapshot cart = cartClient.snapshot(userId);
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new OrderValidationException("Cart for user " + userId + " is empty");
        }

        UUID restaurantId = cart.restaurantId();
        RestaurantSummary restaurant = restaurantClient.getRestaurant(restaurantId);
        if (restaurant == null || !restaurant.isActive()) {
            throw new OrderValidationException("Restaurant " + restaurantId + " is not ACTIVE");
        }

        PricingQuoteRequest quoteRequest = new PricingQuoteRequest(
                userId,
                restaurantId,
                cart.items().stream()
                        .map(i -> new PricingQuoteRequest.QuoteLineItem(i.menuItemId(), i.qty()))
                        .toList(),
                request.promoCode(),
                request.tipCents());
        PricingQuoteResponse quote = pricingClient.quote(quoteRequest);

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .restaurantId(restaurantId)
                .status(OrderStatus.CREATED)
                .subtotalCents(quote.subtotalCents())
                .deliveryFeeCents(quote.deliveryFeeCents())
                .serviceFeeCents(quote.serviceFeeCents())
                .taxCents(quote.taxCents())
                .discountCents(quote.discountCents())
                .tipCents(quote.tipCents())
                .totalCents(quote.totalCents())
                .currency(quote.currency() != null ? quote.currency() : defaultCurrency)
                .createdAt(Instant.now())
                .build();

        for (CartSnapshot.CartSnapshotItem item : cart.items()) {
            order.addItem(OrderItem.builder()
                    .id(UUID.randomUUID())
                    .menuItemId(item.menuItemId())
                    .name(item.name())
                    .qty(item.qty())
                    .unitPriceCents(item.unitPriceCents())
                    .build());
        }

        Order saved = orderRepository.save(order);
        eventProducer.publishOrderCreated(saved);
        log.info("Created order {} for user {} (total={} {})",
                saved.getId(), userId, saved.getTotalCents(), saved.getCurrency());
        return saved;
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Applies a status transition requested by a public caller and emits OrderStatusChanged.
     */
    @Transactional
    public Order updateStatus(UUID id, OrderStatus newStatus) {
        Order order = getOrder(id);
        OrderStatus old = order.getStatus();
        if (old == newStatus) {
            return order;
        }
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        eventProducer.publishOrderStatusChanged(saved, old);
        return saved;
    }

    /**
     * Event-driven transition (idempotent). Used by Kafka listeners; never throws if the
     * order is missing so a stray event does not poison the consumer.
     */
    @Transactional
    public void transitionStatus(UUID id, OrderStatus newStatus) {
        orderRepository.findById(id).ifPresentOrElse(order -> {
            OrderStatus old = order.getStatus();
            if (old == newStatus) {
                return;
            }
            order.setStatus(newStatus);
            Order saved = orderRepository.save(order);
            eventProducer.publishOrderStatusChanged(saved, old);
        }, () -> log.warn("Received status transition to {} for unknown order {}", newStatus, id));
    }
}
