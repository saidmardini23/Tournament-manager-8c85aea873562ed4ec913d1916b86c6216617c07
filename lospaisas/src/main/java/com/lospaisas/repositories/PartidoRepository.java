package com.lospaisas.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.Partido;
import com.lospaisas.models.enums.EstadoPartido;

public interface PartidoRepository extends JpaRepository<Partido, UUID> {

    /**
     * Verifica si un equipo tiene partidos registrados (ya sea como local o como visitante).
     * Se utiliza en EquipoServiceImpl antes de permitir eliminar un equipo.
     */
    boolean existsByEquipoLocalIdOrEquipoVisitanteId(UUID equipoLocalId, UUID equipoVisitanteId);

    /**
     * Obtiene todos los partidos pertenecientes a un torneo a través de la relación Jornada -> Torneo.
     */
    List<Partido> findByJornadaTorneoId(UUID torneoId);

    /**
     * Obtiene los partidos de una jornada específica por su identificador.
     */
    List<Partido> findByJornadaId(UUID jornadaId);

    /**
     * Obtiene los próximos 5 partidos programados ordenados por fecha ascendente.
     * Se utiliza en el Dashboard de administración.
     */
    List<Partido> findTop5ByEstadoOrderByFechaAsc(EstadoPartido estado);
}