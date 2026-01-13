package com.banksystem.controller;

import com.banksystem.dto.*;
import com.banksystem.entity.User;
import com.banksystem.repository.UserRepository;
import com.banksystem.security.JwtUtils;
import com.banksystem.services.AuthService;
import com.banksystem.services.AdminUserManagementService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final AdminUserManagementService adminUserManagementService;

    public AuthController(AuthenticationManager authenticationManager,
                          AuthService authService,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          AdminUserManagementService adminUserManagementService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.adminUserManagementService = adminUserManagementService;
    }

    // 1. PUBLIC SIGNUP (Always creates a CUSTOMER)
    @PostMapping("/signup")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody RegisterRequestDTO signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username is already taken!"));
        }
        authService.registerCustomer(signUpRequest);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully as Customer", null));
    }

    // 2. LOGIN (Generates JWT)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            log.info("Login attempt for user: {}", loginRequest.getUsername());

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authentication successful for user: {}", loginRequest.getUsername());

            // Load ONLY basic user info (no lazy relationships)
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String role = user.getRole().name();
            log.info("User role: {}", role);

            // Use repository query to get specificId WITHOUT loading lazy entities
            Long specificId = userRepository.findSpecificIdByUsername(loginRequest.getUsername());

            // If specificId is null (shouldn't happen but safe fallback), use user ID
            if (specificId == null) {
                log.warn("SpecificId is null for user: {}, using user.id instead", loginRequest.getUsername());
                specificId = user.getId();
            }

            log.info("SpecificId retrieved: {}", specificId);

            // Generate JWT token
            String jwt = jwtUtils.generateToken(user.getUsername(), role, specificId);
            log.info("JWT token generated successfully for user: {}", loginRequest.getUsername());

            return ResponseEntity.ok(new AuthResponseDTO(jwt, user.getUsername(), role, specificId));

        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
}
