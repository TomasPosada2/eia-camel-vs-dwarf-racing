package com.eia.racing.dto.team;

import com.eia.racing.model.CompetitorStatus;
import com.eia.racing.model.CompetitorType;

import java.time.LocalDate;

public record TeamMemberResponse(
        Long id,
        Long competitorId,
        String name,
        String nickname,
        CompetitorType competitorType,
        CompetitorStatus status,
        LocalDate joinedAt
) {
}