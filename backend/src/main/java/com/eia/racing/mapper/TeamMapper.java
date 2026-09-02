package com.eia.racing.mapper;

import com.eia.racing.dto.team.TeamCreateRequest;
import com.eia.racing.dto.team.TeamMemberResponse;
import com.eia.racing.dto.team.TeamResponse;
import com.eia.racing.dto.team.TeamUpdateRequest;
import com.eia.racing.model.Team;
import com.eia.racing.model.TeamMember;
import com.eia.racing.model.TeamStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TeamMapper {

    //convierte lo que llega desde el formulario/API en un Team
    public Team toEntity(TeamCreateRequest request) {
        return Team.builder()
                .name(request.name())
                .description(request.description())
                .coach(request.coach())
                .status(TeamStatus.ACTIVE)
                .build();
    }

    //modifica un equipo existente
    public void updateEntity(Team team, TeamUpdateRequest request) {
        team.setName(request.name());
        team.setDescription(request.description());
        team.setCoach(request.coach());
    }

    public TeamResponse toResponse(Team team, List<TeamMember> members) {

        List<TeamMemberResponse> memberResponses = members.stream()
                .map(this::toMemberResponse)
                .toList();

        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreationDate(),
                team.getStatus(),
                team.getCoach(),
                team.getVictories(),
                team.getDefeats(),
                memberResponses
        );
    }

    public TeamMemberResponse toMemberResponse(TeamMember teamMember) {
        return new TeamMemberResponse(
                teamMember.getId(),
                teamMember.getCompetitor().getId(),
                teamMember.getCompetitor().getName(),
                teamMember.getCompetitor().getNickname(),
                teamMember.getCompetitor().getCompetitorType(),
                teamMember.getCompetitor().getStatus(),
                teamMember.getJoinedAt()
        );
    }
}