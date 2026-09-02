package com.eia.racing.dto.race;

import com.eia.racing.model.RaceStatus;
import jakarta.validation.constraints.NotNull;

public record RaceStatusUpdateRequest(

        @NotNull(message = "Race status is required")
        RaceStatus status
) {
}