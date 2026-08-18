package com.eia.racing.dto.auth;

import com.eia.racing.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {
}
