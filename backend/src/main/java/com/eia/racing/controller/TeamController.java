package com.eia.racing.controller;

import com.eia.racing.dto.team.TeamCreateRequest;
import com.eia.racing.dto.team.TeamResponse;
import com.eia.racing.dto.team.TeamStatusUpdateRequest;
import com.eia.racing.dto.team.TeamUpdateRequest;
import com.eia.racing.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // crear un equipo
    // solo el administrador puede hacerlo
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamResponse> create(
            @Valid @RequestBody TeamCreateRequest request
    ) {

        TeamResponse response = teamService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // listar equipos
    // todos los roles pueden consultar
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER', 'VIEWER')")
    public ResponseEntity<Page<TeamResponse>> list(Pageable pageable) {

        return ResponseEntity.ok(
                teamService.list(pageable)
        );
    }

    // consultar un equipo específico
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER', 'VIEWER')")
    public ResponseEntity<TeamResponse> getById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                teamService.getById(id)
        );
    }

    // modificar la información de un equipo
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TeamUpdateRequest request
    ) {

        return ResponseEntity.ok(
                teamService.update(id, request)
        );
    }

    // cambiar estado:
    // ACTIVE, SUSPENDED o INACTIVE
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TeamStatusUpdateRequest request
    ) {

        return ResponseEntity.ok(
                teamService.updateStatus(id, request.status())
        );
    }

    // desactivar un equipo
    // no se elimina físicamente de la base de datos
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) {

        teamService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    // agregar un competidor al equipo
    @PostMapping("/{teamId}/members/{competitorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamResponse> addMember(
            @PathVariable Long teamId,
            @PathVariable Long competitorId
    ) {

        return ResponseEntity.ok(
                teamService.addMember(teamId, competitorId)
        );
    }

    // retirar un competidor del equipo
    @DeleteMapping("/{teamId}/members/{competitorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamResponse> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long competitorId
    ) {

        return ResponseEntity.ok(
                teamService.removeMember(teamId, competitorId)
        );
    }
}