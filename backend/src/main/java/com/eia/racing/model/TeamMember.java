package com.eia.racing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "team_members",
        //evita que el mismo competidor se agregue dos veces al mismo equipo
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"team_id", "competitor_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(optional = false)
    @JoinColumn(name = "competitor_id", nullable = false)
    private Competitor competitor;

    @Column(nullable = false, updatable = false)
    private LocalDate joinedAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDate.now();
        }
    }
}