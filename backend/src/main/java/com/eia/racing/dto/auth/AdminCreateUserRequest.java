package com.eia.racing.dto.auth;

import com.eia.racing.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Lets an administrator create a user directly with a specific role, unlike public self-registration (always VIEWER). */
public record AdminCreateUserRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotNull(message = "Role is required")
        UserRole role
) {
}
