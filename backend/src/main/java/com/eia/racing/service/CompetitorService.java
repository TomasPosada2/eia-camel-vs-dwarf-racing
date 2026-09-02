package com.eia.racing.service;

import com.eia.racing.dto.competitor.CompetitorCreateRequest;
import com.eia.racing.dto.competitor.CompetitorResponse;
import com.eia.racing.dto.competitor.CompetitorUpdateRequest;
import com.eia.racing.exception.BusinessRuleViolationException;
import com.eia.racing.exception.DuplicateResourceException;
import com.eia.racing.exception.ResourceNotFoundException;
import com.eia.racing.mapper.CompetitorMapper;
import com.eia.racing.model.Competitor;
import com.eia.racing.model.CompetitorStatus;
import com.eia.racing.model.CompetitorType;
import com.eia.racing.repository.CompetitorRepository;
import com.eia.racing.repository.CompetitorSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CompetitorService {

    private final CompetitorRepository competitorRepository;
    private final CompetitorMapper competitorMapper;
    private final AuditService auditService;

    @Transactional
    public CompetitorResponse create(CompetitorCreateRequest request) {

        validateAgeInformation(
                request.dateOfBirth(),
                request.approximateAge()
        );

        assertNicknameAvailable(request.nickname(), null);

        Competitor competitor = competitorMapper.toEntity(request);
        competitor = competitorRepository.save(competitor);

        auditService.record(
                "COMPETITOR_CREATED",
                "Competitor",
                competitor.getId(),
                "Registered new "
                        + competitor.getCompetitorType()
                        + " competitor '"
                        + competitor.getNickname()
                        + "'"
        );

        return competitorMapper.toResponse(competitor);
    }

    @Transactional(readOnly = true)
    public Page<CompetitorResponse> list(
            CompetitorType type,
            CompetitorStatus status,
            String country,
            String search,
            Pageable pageable
    ) {

        Specification<Competitor> spec = Specification
                .where(CompetitorSpecifications.hasType(type))
                .and(CompetitorSpecifications.hasStatus(status))
                .and(CompetitorSpecifications.hasCountry(country))
                .and(CompetitorSpecifications.nameOrNicknameContains(search));

        return competitorRepository
                .findAll(spec, pageable)
                .map(competitorMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CompetitorResponse getById(Long id) {
        return competitorMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public CompetitorResponse update(
            Long id,
            CompetitorUpdateRequest request
    ) {

        Competitor competitor = findOrThrow(id);

        validateAgeInformation(
                request.dateOfBirth(),
                request.approximateAge()
        );

        assertNicknameAvailable(request.nickname(), id);

        competitorMapper.updateEntity(competitor, request);
        competitor = competitorRepository.save(competitor);

        auditService.record(
                "COMPETITOR_UPDATED",
                "Competitor",
                competitor.getId(),
                "Updated details for competitor '"
                        + competitor.getNickname()
                        + "'"
        );

        return competitorMapper.toResponse(competitor);
    }

    @Transactional
    public CompetitorResponse updateStatus(
            Long id,
            CompetitorStatus newStatus
    ) {

        Competitor competitor = findOrThrow(id);

        CompetitorStatus previousStatus = competitor.getStatus();

        competitor.setStatus(newStatus);
        competitor = competitorRepository.save(competitor);

        auditService.record(
                "COMPETITOR_STATUS_CHANGED",
                "Competitor",
                competitor.getId(),
                "Status changed from "
                        + previousStatus
                        + " to "
                        + newStatus,
                previousStatus.name(),
                newStatus.name()
        );

        return competitorMapper.toResponse(competitor);
    }

    /**
     * Competitors are never physically deleted because they may already
     * have official race history. Instead, they are retired.
     */
    @Transactional
    public void retire(Long id) {

        Competitor competitor = findOrThrow(id);

        CompetitorStatus previousStatus = competitor.getStatus();

        competitor.setStatus(CompetitorStatus.RETIRED);
        competitorRepository.save(competitor);

        auditService.record(
                "COMPETITOR_RETIRED",
                "Competitor",
                competitor.getId(),
                "Competitor '"
                        + competitor.getNickname()
                        + "' retired instead of being physically deleted",
                previousStatus.name(),
                CompetitorStatus.RETIRED.name()
        );
    }

    /**
     * A competitor must have either:
     * - date of birth, or
     * - approximate age
     *
     * Exactly one of the two must be provided.
     */
    private void validateAgeInformation(
            LocalDate dateOfBirth,
            Integer approximateAge
    ) {

        if (dateOfBirth == null && approximateAge == null) {
            throw new BusinessRuleViolationException(
                    "Either date of birth or approximate age must be provided"
            );
        }

        if (dateOfBirth != null && approximateAge != null) {
            throw new BusinessRuleViolationException(
                    "Provide either date of birth or approximate age, not both"
            );
        }
    }

    private void assertNicknameAvailable(
            String nickname,
            Long excludingId
    ) {

        competitorRepository
                .findByNicknameIgnoreCase(nickname)
                .ifPresent(existing -> {

                    if (!existing.getId().equals(excludingId)) {
                        throw new DuplicateResourceException(
                                "A competitor with nickname '"
                                        + nickname
                                        + "' already exists"
                        );
                    }
                });
    }

    private Competitor findOrThrow(Long id) {

        return competitorRepository
                .findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.of(
                                "Competitor",
                                id
                        )
                );
    }
}
