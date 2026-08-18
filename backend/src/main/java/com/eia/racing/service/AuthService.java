package com.eia.racing.service;

import com.eia.racing.dto.auth.AuthResponse;
import com.eia.racing.dto.auth.LoginRequest;
import com.eia.racing.dto.auth.RefreshRequest;
import com.eia.racing.dto.auth.RegisterRequest;
import com.eia.racing.dto.auth.UserProfileResponse;
import com.eia.racing.exception.DuplicateResourceException;
import com.eia.racing.exception.ResourceNotFoundException;
import com.eia.racing.mapper.UserMapper;
import com.eia.racing.model.User;
import com.eia.racing.model.UserRole;
import com.eia.racing.repository.UserRepository;
import com.eia.racing.security.JwtService;
import com.eia.racing.security.SecurityUser;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("A user with email " + request.email() + " already exists");
        }

        User user = User.builder()
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(UserRole.VIEWER)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        auditService.record("USER_REGISTERED", "User", user.getId(),
                "New user registered with role " + user.getRole());

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        auditService.record("USER_LOGIN", "User", user.getId(), "User logged in");

        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();
        try {
            if (!jwtService.isRefreshToken(token)) {
                throw new BadCredentialsException("The provided token is not a valid refresh token");
            }
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

            SecurityUser securityUser = new SecurityUser(user);
            if (!jwtService.isTokenValid(token, securityUser)) {
                throw new BadCredentialsException("Refresh token is expired or invalid");
            }

            String newAccessToken = jwtService.generateAccessToken(securityUser);
            return AuthResponse.of(newAccessToken, token, userMapper.toProfileResponse(user));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid or malformed refresh token");
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ResourceNotFoundException.of("User", email));
        return userMapper.toProfileResponse(user);
    }

    private AuthResponse issueTokens(User user) {
        SecurityUser securityUser = new SecurityUser(user);
        String accessToken = jwtService.generateAccessToken(securityUser);
        String refreshToken = jwtService.generateRefreshToken(securityUser);
        return AuthResponse.of(accessToken, refreshToken, userMapper.toProfileResponse(user));
    }
}
