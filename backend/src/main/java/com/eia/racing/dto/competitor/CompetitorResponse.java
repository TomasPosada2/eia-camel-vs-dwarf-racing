package com.eia.racing.dto.competitor;

import com.eia.racing.model.CompetitorStatus;
import com.eia.racing.model.CompetitorType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CompetitorResponse(
        Long id,
        String name,
        String nickname,
        CompetitorType competitorType,
        LocalDate dateOfBirth,
        Integer approximateAge,
        Double weight,
        Double height,
        String countryOrigin,
        CompetitorStatus status,
        LocalDate registrationDate,
        int victories,
        int defeats,
        int completedRaces,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}