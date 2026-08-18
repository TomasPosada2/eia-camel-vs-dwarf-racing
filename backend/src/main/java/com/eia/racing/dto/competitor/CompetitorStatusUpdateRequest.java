package com.eia.racing.dto.competitor;

import com.eia.racing.model.CompetitorStatus;
import jakarta.validation.constraints.NotNull;

public record CompetitorStatusUpdateRequest(
        @NotNull(message = "Status is required")
        CompetitorStatus status
) {
}
