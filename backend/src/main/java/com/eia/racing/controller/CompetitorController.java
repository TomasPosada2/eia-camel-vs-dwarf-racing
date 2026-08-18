package com.eia.racing.controller;

import com.eia.racing.dto.competitor.CompetitorCreateRequest;
import com.eia.racing.dto.competitor.CompetitorResponse;
import com.eia.racing.dto.competitor.CompetitorStatusUpdateRequest;
import com.eia.racing.dto.competitor.CompetitorUpdateRequest;
import com.eia.racing.model.CompetitorStatus;
import com.eia.racing.model.CompetitorType;
import com.eia.racing.service.CompetitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/competitors")
@RequiredArgsConstructor
public class CompetitorController {

    private final CompetitorService competitorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetitorResponse> create(@Valid @RequestBody CompetitorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(competitorService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER', 'VIEWER')")
    public ResponseEntity<Page<CompetitorResponse>> list(
            @RequestParam(required = false) CompetitorType type,
            @RequestParam(required = false) CompetitorStatus status,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(competitorService.list(type, status, country, search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RACE_ORGANIZER', 'VIEWER')")
    public ResponseEntity<CompetitorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(competitorService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetitorResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody CompetitorUpdateRequest request) {
        return ResponseEntity.ok(competitorService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetitorResponse> updateStatus(@PathVariable Long id,
                                                             @Valid @RequestBody CompetitorStatusUpdateRequest request) {
        return ResponseEntity.ok(competitorService.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        competitorService.retire(id);
        return ResponseEntity.noContent().build();
    }
}
