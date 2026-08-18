package com.eia.racing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username/email of the actor. Null for unauthenticated/system actions. */
    private String username;

    @Column(nullable = false, length = 60)
    private String action;

    @Column(nullable = false, length = 60)
    private String entityType;

    private String entityId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 500)
    private String description;

    @Lob
    private String previousValue;

    @Lob
    private String newValue;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
