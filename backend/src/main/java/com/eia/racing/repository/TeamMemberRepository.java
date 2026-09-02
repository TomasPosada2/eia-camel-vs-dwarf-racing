package com.eia.racing.repository;

import com.eia.racing.model.TeamMember;
import com.eia.racing.model.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    //saber si ya se agrego el competidor al mismo equipo
    boolean existsByTeamIdAndCompetitorId(Long teamId, Long competitorId);

    //sacar todos los miembros de un equipo
    List<TeamMember> findByTeamId(Long teamId);

    //encontrar una membresía específica para poder retirarla
    Optional<TeamMember> findByTeamIdAndCompetitorId(Long teamId, Long competitorId);

    //revisar si ese competidor ya está en otro equipo activo
    boolean existsByCompetitorIdAndTeamStatus(Long competitorId, TeamStatus status);
}