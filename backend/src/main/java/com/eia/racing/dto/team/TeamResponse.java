package com.eia.racing.dto.team;

import com.eia.racing.model.TeamStatus;

import java.time.LocalDate;
import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        String description,
        LocalDate creationDate,
        TeamStatus status,
        String coach,
        int victories,
        int defeats,
        List<TeamMemberResponse> members
) {
}