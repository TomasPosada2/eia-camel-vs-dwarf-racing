package com.eia.racing.service;

import com.eia.racing.dto.race.RaceCreateRequest;
import com.eia.racing.dto.race.RaceResponse;
import com.eia.racing.dto.race.RaceUpdateRequest;
import com.eia.racing.exception.BusinessRuleViolationException;
import com.eia.racing.exception.DuplicateResourceException;
import com.eia.racing.exception.ResourceNotFoundException;
import com.eia.racing.mapper.RaceMapper;
import com.eia.racing.model.Race;
import com.eia.racing.model.RaceStatus;
import com.eia.racing.model.User;
import com.eia.racing.repository.RaceRepository;
import com.eia.racing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final RaceRepository raceRepository;
    private final UserRepository userRepository;
    private final RaceMapper raceMapper;
    private final AuditService auditService;

    @Transactional
    public RaceResponse create(RaceCreateRequest request) {

        validateRaceDates(
                request.scheduledDateTime(),
                request.registrationDeadline()
        );

        if (raceRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException(
                    "A race with name '" + request.name() + "' already exists"
            );
        }

        User organizer = getCurrentUser();

        Race race = raceMapper.toEntity(request);
        race.setOrganizer(organizer);

        race = raceRepository.save(race);

        auditService.record(
                "RACE_CREATED",
                "Race",
                race.getId(),
                "Created race '" + race.getName() + "'"
        );

        return raceMapper.toResponse(race);
    }

    @Transactional(readOnly = true)
    public Page<RaceResponse> list(Pageable pageable) {
        return raceRepository.findAll(pageable)
                .map(raceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RaceResponse getById(Long id) {
        return raceMapper.toResponse(findRaceOrThrow(id));
    }

    @Transactional
    public RaceResponse update(Long id, RaceUpdateRequest request) {

        Race race = findRaceOrThrow(id);

        if (race.getStatus() == RaceStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "A completed race cannot be modified"
            );
        }

        if (race.getStatus() == RaceStatus.CANCELLED) {
            throw new BusinessRuleViolationException(
                    "A cancelled race cannot be modified"
            );
        }

        validateRaceDates(
                request.scheduledDateTime(),
                request.registrationDeadline()
        );

        raceRepository.findAll().stream()
                .filter(existing ->
                        existing.getName().equalsIgnoreCase(request.name())
                                && !existing.getId().equals(id)
                )
                .findFirst()
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "A race with name '" + request.name() + "' already exists"
                    );
                });

        raceMapper.updateEntity(race, request);

        race = raceRepository.save(race);

        auditService.record(
                "RACE_UPDATED",
                "Race",
                race.getId(),
                "Updated race '" + race.getName() + "'"
        );

        return raceMapper.toResponse(race);
    }

    @Transactional
    public RaceResponse updateStatus(Long id, RaceStatus newStatus) {

        Race race = findRaceOrThrow(id);
        RaceStatus previousStatus = race.getStatus();

        if (previousStatus == newStatus) {
            throw new BusinessRuleViolationException(
                    "The race is already in status " + newStatus
            );
        }

        validateStatusTransition(previousStatus, newStatus);

        race.setStatus(newStatus);
        race = raceRepository.save(race);

        if (newStatus == RaceStatus.CANCELLED) {
            auditService.record(
                    "RACE_CANCELLED",
                    "Race",
                    race.getId(),
                    "Race '" + race.getName() + "' was cancelled",
                    previousStatus.name(),
                    RaceStatus.CANCELLED.name()
            );
        } else {
            auditService.record(
                    "RACE_STATUS_CHANGED",
                    "Race",
                    race.getId(),
                    "Race status changed from "
                            + previousStatus
                            + " to "
                            + newStatus,
                    previousStatus.name(),
                    newStatus.name()
            );
        }

        return raceMapper.toResponse(race);
    }

    @Transactional
    public void cancel(Long id) {

        Race race = findRaceOrThrow(id);

        if (race.getStatus() == RaceStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "A completed race cannot be cancelled"
            );
        }

        if (race.getStatus() == RaceStatus.CANCELLED) {
            throw new BusinessRuleViolationException(
                    "The race is already cancelled"
            );
        }

        RaceStatus previousStatus = race.getStatus();

        race.setStatus(RaceStatus.CANCELLED);
        raceRepository.save(race);

        auditService.record(
                "RACE_CANCELLED",
                "Race",
                race.getId(),
                "Race '" + race.getName() + "' was cancelled",
                previousStatus.name(),
                RaceStatus.CANCELLED.name()
        );
    }

    private void validateRaceDates(
            LocalDateTime scheduledDateTime,
            LocalDateTime registrationDeadline
    ) {

        if (scheduledDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(
                    "A race cannot be scheduled in the past"
            );
        }

        if (!registrationDeadline.isBefore(scheduledDateTime)) {
            throw new BusinessRuleViolationException(
                    "Registration deadline must be before the race start date"
            );
        }
    }

    private void validateStatusTransition(
            RaceStatus currentStatus,
            RaceStatus newStatus
    ) {

        boolean validTransition = switch (currentStatus) {

            case DRAFT ->
                    newStatus == RaceStatus.OPEN_FOR_REGISTRATION
                            || newStatus == RaceStatus.CANCELLED;

            case OPEN_FOR_REGISTRATION ->
                    newStatus == RaceStatus.CLOSED_FOR_REGISTRATION
                            || newStatus == RaceStatus.CANCELLED;

            case CLOSED_FOR_REGISTRATION ->
                    newStatus == RaceStatus.IN_PROGRESS
                            || newStatus == RaceStatus.CANCELLED;

            case IN_PROGRESS ->
                    newStatus == RaceStatus.COMPLETED
                            || newStatus == RaceStatus.CANCELLED;

            case COMPLETED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new BusinessRuleViolationException(
                    "Invalid race status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }

    private Race findRaceOrThrow(Long id) {
        return raceRepository.findById(id)
                .orElseThrow(() ->
                        ResourceNotFoundException.of("Race", id)
                );
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new BusinessRuleViolationException(
                    "Authenticated organizer is required"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user was not found"
                        )
                );
    }
}