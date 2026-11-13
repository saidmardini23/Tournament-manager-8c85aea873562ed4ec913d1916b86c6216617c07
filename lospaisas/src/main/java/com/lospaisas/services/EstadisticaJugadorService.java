package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.response.EstadisticaJugadorResponse;
import com.lospaisas.models.entity.Equipo;
import com.lospaisas.models.entity.EstadisticaJugador;
import com.lospaisas.models.entity.Jugador;
import com.lospaisas.models.entity.Partido;
import com.lospaisas.models.entity.Torneo;
import com.lospaisas.models.enums.TipoEvento;

public interface EstadisticaJugadorService {

    // --- INTERNOS ---
    EstadisticaJugador actualizarEstadisticas(Jugador jugador, Torneo torneo, Equipo equipo, TipoEvento tipoEvento);

    void registrarPartidoJugado(Partido partido);

    EstadisticaJugador revertirEstadisticas(Jugador jugador, Torneo torneo, Equipo equipo, TipoEvento tipoEvento);

    // --- EXPUESTOS ---
    List<EstadisticaJugadorResponse> obtenerRankingGoleadores(UUID torneoId, UUID equipoId);

    List<EstadisticaJugadorResponse> obtenerRankingAsistencias(UUID torneoId, UUID equipoId);

    // Métodos default: delegan automáticamente al método sobrecargado pasando null
    default List<EstadisticaJugadorResponse> obtenerRankingGoleadores(UUID torneoId) {
        return obtenerRankingGoleadores(torneoId, null);
    }

    default List<EstadisticaJugadorResponse> obtenerRankingAsistencias(UUID torneoId) {
        return obtenerRankingAsistencias(torneoId, null);
    }

    EstadisticaJugadorResponse obtenerEstadisticaPorJugador(UUID jugadorId);
}
