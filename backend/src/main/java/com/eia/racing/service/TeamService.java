package com.eia.racing.service;

import com.eia.racing.dto.team.TeamCreateRequest;
import com.eia.racing.dto.team.TeamResponse;
import com.eia.racing.dto.team.TeamUpdateRequest;
import com.eia.racing.exception.BusinessRuleViolationException;
import com.eia.racing.exception.DuplicateResourceException;
import com.eia.racing.exception.ResourceNotFoundException;
import com.eia.racing.mapper.TeamMapper;
import com.eia.racing.model.Competitor;
import com.eia.racing.model.CompetitorStatus;
import com.eia.racing.model.Team;
import com.eia.racing.model.TeamMember;
import com.eia.racing.model.TeamStatus;
import com.eia.racing.repository.CompetitorRepository;
import com.eia.racing.repository.TeamMemberRepository;
import com.eia.racing.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CompetitorRepository competitorRepository;
    private final TeamMapper teamMapper;
    private final AuditService auditService;

    @Value("${app.team.max-members:5}")
    private int maxMembers;

    @Transactional
    public TeamResponse create(TeamCreateRequest request) {

        if (teamRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException(
                    "A team with name '" + request.name() + "' already exists"
            );
        }

        Team team = teamMapper.toEntity(request);
        team = teamRepository.save(team);

        auditService.record(
                "TEAM_CREATED",
                "Team",
                team.getId(),
                "Created team '" + team.getName() + "'"
        );

        return teamMapper.toResponse(team, List.of());
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> list(Pageable pageable) {
        return teamRepository.findAll(pageable)
                .map(team -> teamMapper.toResponse(
                        team,
                        teamMemberRepository.findByTeamId(team.getId())
                ));
    }

    @Transactional(readOnly = true)
    public TeamResponse getById(Long id) {
        Team team = findTeamOrThrow(id);

        List<TeamMember> members = teamMemberRepository.findByTeamId(id);

        return teamMapper.toResponse(team, members);
    }

    @Transactional
    public TeamResponse update(Long id, TeamUpdateRequest request) {

        Team team = findTeamOrThrow(id);

        teamRepository.findByNameIgnoreCase(request.name())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException(
                                "A team with name '" + request.name() + "' already exists"
                        );
                    }
                });

        teamMapper.updateEntity(team, request);
        team = teamRepository.save(team);

        auditService.record(
                "TEAM_UPDATED",
                "Team",
                team.getId(),
                "Updated team '" + team.getName() + "'"
        );

        return teamMapper.toResponse(
                team,
                teamMemberRepository.findByTeamId(id)
        );
    }

    @Transactional
    public TeamResponse updateStatus(Long id, TeamStatus newStatus) {

        Team team = findTeamOrThrow(id);
        TeamStatus previousStatus = team.getStatus();

        team.setStatus(newStatus);
        team = teamRepository.save(team);

        auditService.record(
                "TEAM_STATUS_CHANGED",
                "Team",
                team.getId(),
                "Team status changed from " + previousStatus + " to " + newStatus,
                previousStatus.name(),
                newStatus.name()
        );

        return teamMapper.toResponse(
                team,
                teamMemberRepository.findByTeamId(id)
        );
    }

    @Transactional
    public void deactivate(Long id) {

        Team team = findTeamOrThrow(id);
        TeamStatus previousStatus = team.getStatus();

        team.setStatus(TeamStatus.INACTIVE);
        teamRepository.save(team);

        auditService.record(
                "TEAM_DEACTIVATED",
                "Team",
                team.getId(),
                "Team '" + team.getName() + "' was deactivated",
                previousStatus.name(),
                TeamStatus.INACTIVE.name()
        );
    }

    @Transactional
    public TeamResponse addMember(Long teamId, Long competitorId) {

        Team team = findTeamOrThrow(teamId);

        if (team.getStatus() != TeamStatus.ACTIVE) {
            throw new BusinessRuleViolationException(
                    "Only active teams can receive new members"
            );
        }

        Competitor competitor = competitorRepository.findById(competitorId)
                .orElseThrow(() ->
                        ResourceNotFoundException.of("Competitor", competitorId)
                );

        if (competitor.getStatus() == CompetitorStatus.RETIRED) {
            throw new BusinessRuleViolationException(
                    "A retired competitor cannot be added to a team"
            );
        }

        if (teamMemberRepository.existsByTeamIdAndCompetitorId(teamId, competitorId)) {
            throw new DuplicateResourceException(
                    "The competitor is already a member of this team"
            );
        }

        if (teamMemberRepository.existsByCompetitorIdAndTeamStatus(
                competitorId,
                TeamStatus.ACTIVE
        )) {
            throw new BusinessRuleViolationException(
                    "The competitor already belongs to another active team"
            );
        }

        List<TeamMember> currentMembers =
                teamMemberRepository.findByTeamId(teamId);

        if (currentMembers.size() >= maxMembers) {
            throw new BusinessRuleViolationException(
                    "The team has reached the maximum number of members"
            );
        }

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .competitor(competitor)
                .build();

        teamMemberRepository.save(teamMember);

        auditService.record(
                "TEAM_MEMBER_ADDED",
                "Team",
                team.getId(),
                "Added competitor '" + competitor.getNickname()
                        + "' to team '" + team.getName() + "'"
        );

        return teamMapper.toResponse(
                team,
                teamMemberRepository.findByTeamId(teamId)
        );
    }

    @Transactional
    public TeamResponse removeMember(Long teamId, Long competitorId) {

        Team team = findTeamOrThrow(teamId);

        TeamMember membership =
                teamMemberRepository
                        .findByTeamIdAndCompetitorId(teamId, competitorId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "The competitor is not a member of this team"
                                )
                        );

        teamMemberRepository.delete(membership);

        auditService.record(
                "TEAM_MEMBER_REMOVED",
                "Team",
                team.getId(),
                "Removed competitor with ID "
                        + competitorId
                        + " from team '"
                        + team.getName()
                        + "'"
        );

        return teamMapper.toResponse(
                team,
                teamMemberRepository.findByTeamId(teamId)
        );
    }

    @Transactional(readOnly = true)
    public void validateEligibleForRace(Long teamId) {

        Team team = findTeamOrThrow(teamId);

        if (team.getStatus() != TeamStatus.ACTIVE) {
            throw new BusinessRuleViolationException(
                    "The team is not active and cannot participate in races"
            );
        }

        List<TeamMember> members =
                teamMemberRepository.findByTeamId(teamId);

        if (members.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "The team must have at least one member before entering a race"
            );
        }

        boolean hasInvalidMember = members.stream()
                .anyMatch(member ->
                        member.getCompetitor().getStatus()
                                != CompetitorStatus.ACTIVE
                );

        if (hasInvalidMember) {
            throw new BusinessRuleViolationException(
                    "All team members must be active to participate in a race"
            );
        }
    }

    private Team findTeamOrThrow(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.of("Team", id)
                );
    }
}