package com.quickbite.identity.dto;

import com.quickbite.identity.domain.Role;
import com.quickbite.identity.domain.User;
import java.time.Instant;
import java.util.UUID;

/**
 * Public-facing user view returned by {@code /api/users/**} and embedded in the
 * login response. Never exposes the password hash.
 */
public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Role role,
        boolean active,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt());
    }
}
