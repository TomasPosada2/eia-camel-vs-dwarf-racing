package com.eia.racing.mapper;

import com.eia.racing.dto.race.RaceCreateRequest;
import com.eia.racing.dto.race.RaceResponse;
import com.eia.racing.dto.race.RaceUpdateRequest;
import com.eia.racing.model.Race;
import com.eia.racing.model.RaceStatus;
import org.springframework.stereotype.Component;

@Component
public class RaceMapper {

    public Race toEntity(RaceCreateRequest request) {
        return Race.builder()
                .name(request.name())
                .description(request.description())
                .scheduledDateTime(request.scheduledDateTime())
                .startLocation(request.startLocation())
                .endLocation(request.endLocation())
                .distanceMeters(request.distanceMeters())
                .maxParticipants(request.maxParticipants())
                .type(request.type())
                .status(RaceStatus.DRAFT)
                .registrationDeadline(request.registrationDeadline())
                .build();
    }

    public void updateEntity(Race race, RaceUpdateRequest request) {
        race.setName(request.name());
        race.setDescription(request.description());
        race.setScheduledDateTime(request.scheduledDateTime());
        race.setStartLocation(request.startLocation());
        race.setEndLocation(request.endLocation());
        race.setDistanceMeters(request.distanceMeters());
        race.setMaxParticipants(request.maxParticipants());
        race.setType(request.type());
        race.setRegistrationDeadline(request.registrationDeadline());
    }

    public RaceResponse toResponse(Race race) {
        return new RaceResponse(
                race.getId(),
                race.getName(),
                race.getDescription(),
                race.getScheduledDateTime(),
                race.getStartLocation(),
                race.getEndLocation(),
                race.getDistanceMeters(),
                race.getMaxParticipants(),
                race.getType(),
                race.getStatus(),
                race.getOrganizer().getId(),
                race.getOrganizer().getEmail(),
                race.getRegistrationDeadline(),
                race.getCreatedAt(),
                race.getUpdatedAt()
        );
    }
}