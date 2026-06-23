package com.quickbite.driver.domain;

/**
 * Lifecycle of a courier with respect to delivery dispatch.
 *
 * <ul>
 *   <li>{@code OFFLINE} — not accepting work.</li>
 *   <li>{@code AVAILABLE} — online and eligible to be assigned a delivery.</li>
 *   <li>{@code ON_DELIVERY} — currently fulfilling a delivery; not assignable.</li>
 * </ul>
 */
public enum DriverStatus {
    OFFLINE,
    AVAILABLE,
    ON_DELIVERY
}
