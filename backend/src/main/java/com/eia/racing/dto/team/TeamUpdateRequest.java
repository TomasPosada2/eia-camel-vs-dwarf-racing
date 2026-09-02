package com.eia.racing.dto.team;

import jakarta.validation.constraints.NotBlank;

public record TeamUpdateRequest(

        @NotBlank(message = "Team name is required")
        String name,

        String description,

        @NotBlank(message = "Coach is required")
        String coach
) {
}