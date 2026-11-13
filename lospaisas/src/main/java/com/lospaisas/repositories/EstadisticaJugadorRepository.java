package com.lospaisas.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.EstadisticaJugador;

public interface EstadisticaJugadorRepository extends JpaRepository<EstadisticaJugador, UUID> {

    List<EstadisticaJugador> findByJugadorId(UUID jugadorId);

    List<EstadisticaJugador> findByJugadorIdAndTorneoId(UUID jugadorId, UUID torneoId);

    List<EstadisticaJugador> findTop50ByTorneoIdOrderByGolesDesc(UUID torneoId);

    List<EstadisticaJugador> findTop50ByTorneoIdOrderByAsistenciasDesc(UUID torneoId);

}