package com.lospaisas.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.dto.DtoMapper;
import com.lospaisas.dto.response.EstadisticaJugadorResponse;
import com.lospaisas.models.entity.Equipo;
import com.lospaisas.models.entity.EstadisticaJugador;
import com.lospaisas.models.entity.Jugador;
import com.lospaisas.models.entity.Partido;
import com.lospaisas.models.entity.Torneo;
import com.lospaisas.models.enums.TipoEvento;
import com.lospaisas.repositories.EstadisticaJugadorRepository;
import com.lospaisas.repositories.JugadorRepository;
import com.lospaisas.services.EstadisticaJugadorService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EstadisticaJugadorServiceImpl implements EstadisticaJugadorService {

    private final EstadisticaJugadorRepository estadisticaJugadorRepository;
    private final JugadorRepository jugadorRepository;

    public EstadisticaJugadorServiceImpl(
            EstadisticaJugadorRepository estadisticaJugadorRepository,
            JugadorRepository jugadorRepository) {
        this.estadisticaJugadorRepository = estadisticaJugadorRepository;
        this.jugadorRepository = jugadorRepository;
    }

    @Override
    public EstadisticaJugador actualizarEstadisticas(
            Jugador jugador,
            Torneo torneo,
            Equipo equipo,
            TipoEvento tipoEvento) {

        EstadisticaJugador estadistica = obtenerOCrearEstadistica(jugador, torneo, equipo);

        switch (tipoEvento) {
            case GOL_JUGADA, GOL_CABEZA, GOL_PENAL, GOL_TIRO_LIBRE ->
                estadistica.setGoles(estadistica.getGoles() + 1);

            case ASISTENCIA ->
                estadistica.setAsistencias(estadistica.getAsistencias() + 1);

            case TARJETA_AMARILLA, DOBLE_AMARILLA ->
                estadistica.setTarjetasAmarillas(estadistica.getTarjetasAmarillas() + 1);

            case TARJETA_ROJA, EXPULSION_DIRECTA ->
                estadistica.setTarjetasRojas(estadistica.getTarjetasRojas() + 1);

            default -> {
            }
        }

        return estadisticaJugadorRepository.save(estadistica);
    }

    @Override
    public void registrarPartidoJugado(Partido partido) {
        Torneo torneo = partido.getJornada().getTorneo();

        List<Jugador> jugadoresLocal = jugadorRepository.findByEquipoId(partido.getEquipoLocal().getId());
        List<Jugador> jugadoresVisitante = jugadorRepository.findByEquipoId(partido.getEquipoVisitante().getId());

        procesarPartidosJugados(jugadoresLocal, torneo);
        procesarPartidosJugados(jugadoresVisitante, torneo);
    }

    @Override
    public EstadisticaJugador revertirEstadisticas(
            Jugador jugador,
            Torneo torneo,
            Equipo equipo,
            TipoEvento tipoEvento) {

        EstadisticaJugador estadistica = obtenerOCrearEstadistica(jugador, torneo, equipo);

        switch (tipoEvento) {
            case GOL_JUGADA, GOL_CABEZA, GOL_PENAL, GOL_TIRO_LIBRE -> {
                if (estadistica.getGoles() > 0)
                    estadistica.setGoles(estadistica.getGoles() - 1);
            }
            case ASISTENCIA -> {
                if (estadistica.getAsistencias() > 0)
                    estadistica.setAsistencias(estadistica.getAsistencias() - 1);
            }
            case TARJETA_AMARILLA, DOBLE_AMARILLA -> {
                if (estadistica.getTarjetasAmarillas() > 0)
                    estadistica.setTarjetasAmarillas(estadistica.getTarjetasAmarillas() - 1);
            }
            case TARJETA_ROJA, EXPULSION_DIRECTA -> {
                if (estadistica.getTarjetasRojas() > 0)
                    estadistica.setTarjetasRojas(estadistica.getTarjetasRojas() - 1);
            }
            default -> {
            }
        }

        return estadisticaJugadorRepository.save(estadistica);
    }

    @Override
    public List<EstadisticaJugadorResponse> obtenerRankingGoleadores(UUID torneoId, UUID equipoId) {
        if (torneoId == null) {
            return Collections.emptyList();
        }
        List<EstadisticaJugadorResponse> ranking = estadisticaJugadorRepository
                .findTop50ByTorneoIdOrderByGolesDesc(torneoId).stream()
                .map(DtoMapper::toResponse)
                .toList();

        if (equipoId != null) {
            return ranking.stream()
                    .filter(g -> equipoId.equals(g.equipoId()))
                    .toList();
        }
        return ranking;
    }

    @Override
    public List<EstadisticaJugadorResponse> obtenerRankingAsistencias(UUID torneoId, UUID equipoId) {
        if (torneoId == null) {
            return Collections.emptyList();
        }
        List<EstadisticaJugadorResponse> ranking = estadisticaJugadorRepository
                .findTop50ByTorneoIdOrderByAsistenciasDesc(torneoId).stream()
                .map(DtoMapper::toResponse)
                .toList();

        if (equipoId != null) {
            return ranking.stream()
                    .filter(a -> equipoId.equals(a.equipoId()))
                    .toList();
        }
        return ranking;
    }

    private EstadisticaJugador obtenerOCrearEstadistica(Jugador jugador, Torneo torneo, Equipo equipo) {
        return estadisticaJugadorRepository
                .findByJugadorIdAndTorneoId(jugador.getId(), torneo.getId())
                .stream()
                .findFirst()
                .orElseGet(() -> crearEstadistica(jugador, torneo, equipo));
    }

    private EstadisticaJugador crearEstadistica(Jugador jugador, Torneo torneo, Equipo equipo) {
        EstadisticaJugador estadistica = new EstadisticaJugador();
        estadistica.setJugador(jugador);
        estadistica.setTorneo(torneo);
        estadistica.setEquipo(equipo);
        estadistica.setGoles(0);
        estadistica.setAsistencias(0);
        estadistica.setTarjetasAmarillas(0);
        estadistica.setTarjetasRojas(0);
        estadistica.setPartidosJugados(0);

        return estadisticaJugadorRepository.save(estadistica);
    }

    private void procesarPartidosJugados(List<Jugador> jugadores, Torneo torneo) {
        for (Jugador jugador : jugadores) {
            EstadisticaJugador estadistica = obtenerOCrearEstadistica(jugador, torneo, jugador.getEquipo());
            estadistica.setPartidosJugados(estadistica.getPartidosJugados() + 1);
            estadisticaJugadorRepository.save(estadistica);
        }
    }

    @Override
    public EstadisticaJugadorResponse obtenerEstadisticaPorJugador(UUID jugadorId) {
        return estadisticaJugadorRepository.findByJugadorId(jugadorId).stream()
                .findFirst()
                .map(DtoMapper::toResponse)
                .orElse(null);
    }
}