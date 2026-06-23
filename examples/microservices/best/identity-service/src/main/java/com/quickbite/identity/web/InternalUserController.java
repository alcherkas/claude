package com.quickbite.identity.web;

import com.quickbite.identity.domain.Role;
import com.quickbite.identity.dto.InternalUserResponse;
import com.quickbite.identity.dto.ValidationResponse;
import com.quickbite.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints under {@code /internal/**}. Never exposed through the gateway;
 * called by sibling services' Feign clients to resolve and validate users.
 */
@RestController
@RequestMapping("/internal/users")
@Tag(name = "Internal")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Fetch compact user details (internal)")
    @GetMapping("/{id}")
    public ResponseEntity<InternalUserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(InternalUserResponse.from(userService.getById(id)));
    }

    @Operation(summary = "Validate that a user exists, is active, and optionally has a role")
    @GetMapping("/{id}/validate")
    public ResponseEntity<ValidationResponse> validate(
            @PathVariable UUID id,
            @RequestParam(name = "role", required = false) Role role) {
        return ResponseEntity.ok(new ValidationResponse(userService.validate(id, role)));
    }
}
