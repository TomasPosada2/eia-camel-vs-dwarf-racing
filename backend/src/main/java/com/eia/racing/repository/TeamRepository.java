package com.eia.racing.repository;

import com.eia.racing.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}