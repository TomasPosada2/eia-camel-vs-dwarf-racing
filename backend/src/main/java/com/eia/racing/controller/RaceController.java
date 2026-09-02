package com.eia.racing.controller;

import com.eia.racing.dto.race.RaceCreateRequest;
import com.eia.racing.dto.race.RaceResponse;
import com.eia.racing.dto.race.RaceStatusUpdateRequest;
import com.eia.racing.dto.race.RaceUpdateRequest;
import com.eia.racing.service.RaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/races")
@RequiredArgsConstructor
public class RaceController {

    private final RaceService raceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER')")
    public ResponseEntity<RaceResponse> create(
            @Valid @RequestBody RaceCreateRequest request
    ) {

        RaceResponse response = raceService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER', 'VIEWER')")
    public ResponseEntity<Page<RaceResponse>> list(
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                raceService.list(pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER', 'VIEWER')")
    public ResponseEntity<RaceResponse> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                raceService.getById(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER')")
    public ResponseEntity<RaceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RaceUpdateRequest request
    ) {

        return ResponseEntity.ok(
                raceService.update(id, request)
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER')")
    public ResponseEntity<RaceResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody RaceStatusUpdateRequest request
    ) {

        return ResponseEntity.ok(
                raceService.updateStatus(id, request.status())
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER')")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id
    ) {

        raceService.cancel(id);

        return ResponseEntity.noContent().build();
    }
}