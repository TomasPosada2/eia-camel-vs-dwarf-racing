package com.eia.racing.service;

import com.eia.racing.model.AuditLog;
import com.eia.racing.repository.AuditLogRepository;
import com.eia.racing.repository.AuditLogSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reusable audit-trail component. Persona 2 and Persona 3 can inject this service
 * into their own Team/Race/Registration/Result services to log actions the same way.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(String action, String entityType, Object entityId, String description) {
        record(action, entityType, entityId, description, null, null);
    }

    public void record(String action, String entityType, Object entityId, String description,
                        String previousValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .username(currentUsername())
                .action(action)
                .entityType(entityType)
                .entityId(entityId == null ? null : String.valueOf(entityId))
                .description(description)
                .previousValue(previousValue)
                .newValue(newValue)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> search(String entityType, String action, String username, Pageable pageable) {
        Specification<AuditLog> spec = Specification
                .where(AuditLogSpecifications.hasEntityType(entityType))
                .and(AuditLogSpecifications.hasAction(action))
                .and(AuditLogSpecifications.hasUsername(username));
        return auditLogRepository.findAll(spec, pageable);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "system";
        }
        return authentication.getName();
    }
}
