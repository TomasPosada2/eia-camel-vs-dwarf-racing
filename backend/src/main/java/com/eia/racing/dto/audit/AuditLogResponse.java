package com.eia.racing.dto.audit;

import com.eia.racing.model.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String username,
        String action,
        String entityType,
        String entityId,
        LocalDateTime timestamp,
        String description,
        String previousValue,
        String newValue
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUsername(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getTimestamp(),
                log.getDescription(),
                log.getPreviousValue(),
                log.getNewValue());
    }
}
