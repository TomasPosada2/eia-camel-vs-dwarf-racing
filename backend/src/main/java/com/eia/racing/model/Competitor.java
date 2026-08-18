package com.eia.racing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "competitors", uniqueConstraints = @UniqueConstraint(columnNames = "nickname"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompetitorType competitorType;

    private LocalDate dateOfBirth;

    private Integer approximateAge;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double height;

    private String countryOrigin;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private CompetitorStatus status = CompetitorStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDate registrationDate;

    /**
     * References Team.id once Persona 2 introduces the Team entity.
     * Kept as a plain column (no JPA relationship) so this module has no
     * compile-time dependency on a class that does not exist yet.
     */
    private Long teamId;

    @Builder.Default
    @Column(nullable = false)
    private int victories = 0;

    @Builder.Default
    @Column(nullable = false)
    private int defeats = 0;

    @Builder.Default
    @Column(nullable = false)
    private int completedRaces = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.registrationDate == null) {
            this.registrationDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isEligibleForRace() {
        return this.status == CompetitorStatus.ACTIVE;
    }
}
