package com.quickbite.driver.service;

import java.util.UUID;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException(UUID id) {
        super("Driver " + id + " does not exist");
    }
}
