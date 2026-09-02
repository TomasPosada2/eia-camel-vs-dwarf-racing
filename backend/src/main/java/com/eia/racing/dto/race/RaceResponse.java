package com.eia.racing.dto.race;

import com.eia.racing.model.RaceStatus;
import com.eia.racing.model.RaceType;

import java.time.LocalDateTime;

public record RaceResponse(

        Long id,

        String name,

        String description,

        LocalDateTime scheduledDateTime,

        String startLocation,

        String endLocation,

        Double distanceMeters,

        Integer maxParticipants,

        RaceType type,

        RaceStatus status,

        Long organizerId,

        String organizerEmail,

        LocalDateTime registrationDeadline,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}