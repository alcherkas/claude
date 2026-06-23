package com.quickbite.identity.domain;

/**
 * Account role. Used both for authorization and as the JWT {@code role} claim.
 */
public enum Role {
    CUSTOMER,
    RESTAURANT_OWNER,
    COURIER,
    ADMIN
}
