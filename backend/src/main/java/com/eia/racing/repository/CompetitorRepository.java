package com.eia.racing.repository;

import com.eia.racing.model.Competitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CompetitorRepository extends JpaRepository<Competitor, Long>, JpaSpecificationExecutor<Competitor> {

    Optional<Competitor> findByNicknameIgnoreCase(String nickname);

    boolean existsByNicknameIgnoreCase(String nickname);
}
