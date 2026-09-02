package com.eia.racing.dto.team;

import com.eia.racing.model.TeamStatus;
import jakarta.validation.constraints.NotNull;

public record TeamStatusUpdateRequest(

        @NotNull(message = "Status is required")
        TeamStatus status
) {
}