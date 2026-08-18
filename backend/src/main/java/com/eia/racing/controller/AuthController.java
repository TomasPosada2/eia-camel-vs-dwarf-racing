package com.eia.racing.controller;

import com.eia.racing.dto.auth.AuthResponse;
import com.eia.racing.dto.auth.LoginRequest;
import com.eia.racing.dto.auth.RefreshRequest;
import com.eia.racing.dto.auth.RegisterRequest;
import com.eia.racing.dto.auth.UserProfileResponse;
import com.eia.racing.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> profile(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(authService.getProfile(principal.getUsername()));
    }
}
