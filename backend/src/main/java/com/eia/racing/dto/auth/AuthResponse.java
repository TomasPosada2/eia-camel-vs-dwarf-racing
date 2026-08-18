package com.eia.racing.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserProfileResponse user
) {
    public static AuthResponse of(String accessToken, String refreshToken, UserProfileResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", user);
    }
}
