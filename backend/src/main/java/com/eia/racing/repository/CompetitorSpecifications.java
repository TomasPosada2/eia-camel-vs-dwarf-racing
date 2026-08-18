package com.eia.racing.repository;

import com.eia.racing.model.Competitor;
import com.eia.racing.model.CompetitorStatus;
import com.eia.racing.model.CompetitorType;
import org.springframework.data.jpa.domain.Specification;

public final class CompetitorSpecifications {

    private CompetitorSpecifications() {
    }

    public static Specification<Competitor> hasType(CompetitorType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("competitorType"), type);
    }

    public static Specification<Competitor> hasStatus(CompetitorStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Competitor> hasCountry(String country) {
        return (root, query, cb) -> (country == null || country.isBlank())
                ? null
                : cb.equal(cb.lower(root.get("countryOrigin")), country.toLowerCase());
    }

    public static Specification<Competitor> nameOrNicknameContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("nickname")), pattern));
        };
    }
}
