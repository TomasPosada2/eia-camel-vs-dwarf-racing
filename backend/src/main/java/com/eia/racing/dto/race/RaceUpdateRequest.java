package com.eia.racing.dto.race;

import com.eia.racing.model.RaceType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record RaceUpdateRequest(

        @NotBlank(message = "Race name is required")
        String name,

        String description,

        @NotNull(message = "Scheduled date and time are required")
        @Future(message = "The race must be scheduled in the future")
        LocalDateTime scheduledDateTime,

        @NotBlank(message = "Start location is required")
        String startLocation,

        @NotBlank(message = "End location is required")
        String endLocation,

        @NotNull(message = "Distance is required")
        @Positive(message = "Distance must be greater than zero")
        Double distanceMeters,

        @NotNull(message = "Maximum participants is required")
        @Min(value = 2, message = "Maximum participants must be at least 2")
        Integer maxParticipants,

        @NotNull(message = "Race type is required")
        RaceType type,

        @NotNull(message = "Registration deadline is required")
        @Future(message = "Registration deadline must be in the future")
        LocalDateTime registrationDeadline
) {
}