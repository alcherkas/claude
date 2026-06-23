package com.quickbite.identity.service;

import com.quickbite.identity.config.JwtService;
import com.quickbite.identity.domain.Role;
import com.quickbite.identity.domain.User;
import com.quickbite.identity.dto.LoginRequest;
import com.quickbite.identity.dto.LoginResponse;
import com.quickbite.identity.dto.RegisterRequest;
import com.quickbite.identity.dto.UserResponse;
import com.quickbite.identity.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core identity logic: registration, credential checks, JWT issuance, and lookups.
 */
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException("Email already registered: " + email);
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(request.role())
                .active(true)
                .createdAt(Instant.now())
                .build();
        User saved = userRepository.save(user);
        log.info("Registered user {} with role {}", saved.getId(), saved.getRole());
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        JwtService.IssuedToken issued = jwtService.issue(user);
        return new LoginResponse(issued.token(), issued.expiresAt(), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public UserResponse getResponseById(UUID id) {
        return UserResponse.from(getById(id));
    }

    /**
     * True when the user exists, is active, and (optionally) matches the required role.
     * Used by the internal validation endpoint.
     */
    @Transactional(readOnly = true)
    public boolean validate(UUID id, Role requiredRole) {
        return userRepository.findById(id)
                .filter(User::isActive)
                .filter(u -> requiredRole == null || u.getRole() == requiredRole)
                .isPresent();
    }
}
