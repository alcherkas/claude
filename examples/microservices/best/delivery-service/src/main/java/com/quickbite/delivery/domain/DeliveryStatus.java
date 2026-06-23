package com.quickbite.delivery.domain;

public enum DeliveryStatus {
    PENDING,
    ASSIGNED,
    EN_ROUTE_TO_PICKUP,
    PICKED_UP,
    EN_ROUTE_TO_CUSTOMER,
    DELIVERED,
    FAILED
}
