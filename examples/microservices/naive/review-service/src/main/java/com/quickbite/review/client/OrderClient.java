package com.quickbite.review.client;

import com.quickbite.review.dto.OrderSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/** Mandatory dependency: verifies the order is DELIVERED and belongs to the user. */
@FeignClient(name = "orderClient", url = "${clients.order.url}", fallback = OrderClientFallback.class)
public interface OrderClient {

    @GetMapping("/internal/orders/{id}")
    OrderSummary getOrder(@PathVariable("id") UUID id);
}
