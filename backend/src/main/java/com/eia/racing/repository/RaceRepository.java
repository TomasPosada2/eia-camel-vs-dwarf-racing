package com.eia.racing.repository;

import com.eia.racing.model.Race;
import com.eia.racing.model.RaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaceRepository extends JpaRepository<Race, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Race> findByStatus(RaceStatus status);
}