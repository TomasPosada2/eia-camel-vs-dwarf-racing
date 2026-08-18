package com.eia.racing.dto.auth;

import com.eia.racing.model.UserRole;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String email,
        String fullName,
        UserRole role,
        boolean enabled,
        LocalDateTime createdAt
) {
}
