package com.eia.racing.dto.competitor;

import com.eia.racing.model.CompetitorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CompetitorCreateRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Nickname is required")
        String nickname,

        @NotNull(message = "Competitor type is required")
        CompetitorType competitorType,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Positive(message = "Approximate age must be positive")
        Integer approximateAge,

        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be a positive number")
        Double weight,

        @NotNull(message = "Height is required")
        @Positive(message = "Height must be a positive number")
        Double height,

        String countryOrigin,

        Long teamId
) {
}
