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
        name = "teams",
        uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //id del equipo

    @Column(nullable = false, unique = true)
    private String name; //nombre único

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDate creationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TeamStatus status = TeamStatus.ACTIVE;

    private String coach;

    //estadíticas del equipo
    @Builder.Default
    @Column(nullable = false)
    private int victories = 0;

    @Builder.Default
    @Column(nullable = false)
    private int defeats = 0;

    @PrePersist
    protected void onCreate() {
        if (creationDate == null) {
            creationDate = LocalDate.now();
        }
    }
}