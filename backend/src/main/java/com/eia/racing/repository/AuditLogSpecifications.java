package com.eia.racing.repository;

import com.eia.racing.model.AuditLog;
import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> hasEntityType(String entityType) {
        return (root, query, cb) -> (entityType == null || entityType.isBlank())
                ? null
                : cb.equal(cb.lower(root.get("entityType")), entityType.toLowerCase());
    }

    public static Specification<AuditLog> hasAction(String action) {
        return (root, query, cb) -> (action == null || action.isBlank())
                ? null
                : cb.equal(cb.lower(root.get("action")), action.toLowerCase());
    }

    public static Specification<AuditLog> hasUsername(String username) {
        return (root, query, cb) -> (username == null || username.isBlank())
                ? null
                : cb.equal(cb.lower(root.get("username")), username.toLowerCase());
    }
}
