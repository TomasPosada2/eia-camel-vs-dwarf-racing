package com.eia.racing.mapper;

import com.eia.racing.dto.competitor.CompetitorCreateRequest;
import com.eia.racing.dto.competitor.CompetitorResponse;
import com.eia.racing.dto.competitor.CompetitorUpdateRequest;
import com.eia.racing.model.Competitor;
import com.eia.racing.model.CompetitorStatus;
import org.springframework.stereotype.Component;

@Component
public class CompetitorMapper {

    public Competitor toEntity(CompetitorCreateRequest request) {
        return Competitor.builder()
                .name(request.name())
                .nickname(request.nickname())
                .competitorType(request.competitorType())
                .dateOfBirth(request.dateOfBirth())
                .approximateAge(request.approximateAge())
                .weight(request.weight())
                .height(request.height())
                .countryOrigin(request.countryOrigin())
                .teamId(request.teamId())
                .status(CompetitorStatus.ACTIVE)
                .build();
    }

    public void updateEntity(Competitor competitor, CompetitorUpdateRequest request) {
        competitor.setName(request.name());
        competitor.setNickname(request.nickname());
        competitor.setCompetitorType(request.competitorType());
        competitor.setDateOfBirth(request.dateOfBirth());
        competitor.setApproximateAge(request.approximateAge());
        competitor.setWeight(request.weight());
        competitor.setHeight(request.height());
        competitor.setCountryOrigin(request.countryOrigin());
        competitor.setTeamId(request.teamId());
    }

    public CompetitorResponse toResponse(Competitor competitor) {
        return new CompetitorResponse(
                competitor.getId(),
                competitor.getName(),
                competitor.getNickname(),
                competitor.getCompetitorType(),
                competitor.getDateOfBirth(),
                competitor.getApproximateAge(),
                competitor.getWeight(),
                competitor.getHeight(),
                competitor.getCountryOrigin(),
                competitor.getStatus(),
                competitor.getRegistrationDate(),
                competitor.getTeamId(),
                competitor.getVictories(),
                competitor.getDefeats(),
                competitor.getCompletedRaces(),
                competitor.getCreatedAt(),
                competitor.getUpdatedAt());
    }
}
