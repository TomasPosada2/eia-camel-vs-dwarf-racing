package com.eia.racing.dto.auth;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "Enabled flag is required")
        Boolean enabled
) {
}
