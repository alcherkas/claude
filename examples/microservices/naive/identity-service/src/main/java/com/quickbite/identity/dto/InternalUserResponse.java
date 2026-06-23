package com.quickbite.identity.dto;

import com.quickbite.identity.domain.Role;
import com.quickbite.identity.domain.User;
import java.util.UUID;

/**
 * Compact user view served on {@code GET /internal/users/{id}} for sibling services.
 */
public record InternalUserResponse(
        UUID id,
        String email,
        String fullName,
        Role role,
        boolean active
) {
    public static InternalUserResponse from(User user) {
        return new InternalUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isActive());
    }
}
