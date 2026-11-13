package com.lospaisas.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.dto.DtoMapper;
import com.lospaisas.exceptions.RecursoNoEncontradoException;
import com.lospaisas.exceptions.ValidacionException;
import com.lospaisas.dto.request.EventoPartidoRequest;
import com.lospaisas.dto.request.EventoPartidoUpdateRequest;
import com.lospaisas.dto.response.EventoPartidoResponse;
import com.lospaisas.models.entity.EventoPartido;
import com.lospaisas.models.entity.Jugador;
import com.lospaisas.models.entity.Partido;
import com.lospaisas.models.enums.TipoEvento;
import com.lospaisas.repositories.EventoPartidoRepository;
import com.lospaisas.repositories.JugadorRepository;
import com.lospaisas.repositories.PartidoRepository;
import com.lospaisas.services.EstadisticaJugadorService;
import com.lospaisas.services.EventoPartidoService;
import com.lospaisas.services.PartidoService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EventoPartidoServiceImpl implements EventoPartidoService {

    private final EventoPartidoRepository eventoPartidoRepository;
    private final PartidoRepository partidoRepository;
    private final JugadorRepository jugadorRepository;
    private final PartidoService partidoService;
    private final EstadisticaJugadorService estadisticaJugadorService;

    public EventoPartidoServiceImpl(
            EventoPartidoRepository eventoPartidoRepository,
            PartidoRepository partidoRepository,
            JugadorRepository jugadorRepository,
            PartidoService partidoService,
            EstadisticaJugadorService estadisticaJugadorService) {
        this.eventoPartidoRepository = eventoPartidoRepository;
        this.partidoRepository = partidoRepository;
        this.jugadorRepository = jugadorRepository;
        this.partidoService = partidoService;
        this.estadisticaJugadorService = estadisticaJugadorService;
    }

    /**
     * Registra un evento en el partido, actualiza el marcador si es gol
     * y recalcula las estadísticas del jugador.
     */
    @Override
    public EventoPartidoResponse registrarEvento(EventoPartidoRequest request) {

        UUID partidoId = request.partidoId();
        UUID jugadorId = request.jugadorId();
        TipoEvento tipoEvento = request.tipoEvento();
        int minuto = request.minuto();

        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado"));

        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Jugador no encontrado"));

        validarMinuto(minuto);

        boolean esLocal = jugador.getEquipo().getId().equals(partido.getEquipoLocal().getId());
        boolean esVisitante = jugador.getEquipo().getId().equals(partido.getEquipoVisitante().getId());

        if (!esLocal && !esVisitante) {
            throw new ValidacionException("El jugador no pertenece a ninguno de los equipos del partido");
        }

        EventoPartido evento = new EventoPartido();
        evento.setPartido(partido);
        evento.setJugador(jugador);
        evento.setTipoEvento(tipoEvento);
        evento.setMinuto(minuto);

        EventoPartido eventoGuardado = eventoPartidoRepository.save(evento);

        aplicarEfectosEvento(partido, jugador, tipoEvento, esLocal);

        return DtoMapper.toResponse(eventoGuardado);
    }

    /**
     * Lista todos los eventos asociados a un partido validando su existencia.
     */
    @Override
    public List<EventoPartidoResponse> listarPorPartido(UUID partidoId) {
        if (!partidoRepository.existsById(partidoId)) {
            throw new RecursoNoEncontradoException("Partido no encontrado");
        }
        return eventoPartidoRepository.findByPartidoId(partidoId).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    /**
     * Actualiza un evento existente, revirtiendo sus efectos anteriores
     * y aplicando los nuevos cambios.
     */
    @Override
    public EventoPartidoResponse actualizarEvento(
            UUID eventoId,
            EventoPartidoUpdateRequest request) {

        TipoEvento nuevoTipoEvento = request.tipoEvento();
        int nuevoMinuto = request.minuto();

        EventoPartido evento = eventoPartidoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));

        validarMinuto(nuevoMinuto);

        Partido partido = evento.getPartido();
        Jugador jugador = evento.getJugador();

        boolean esLocal = jugador.getEquipo().getId().equals(partido.getEquipoLocal().getId());
        boolean esVisitante = jugador.getEquipo().getId().equals(partido.getEquipoVisitante().getId());

        if (!esLocal && !esVisitante) {
            throw new ValidacionException("El jugador ya no pertenece a ninguno de los equipos del partido");
        }

        // Revertir efectos del evento anterior antes de aplicar los nuevos
        revertirEfectosEvento(partido, jugador, evento.getTipoEvento(), esLocal);

        evento.setTipoEvento(nuevoTipoEvento);
        evento.setMinuto(nuevoMinuto);

        EventoPartido eventoActualizado = eventoPartidoRepository.save(evento);

        aplicarEfectosEvento(partido, jugador, nuevoTipoEvento, esLocal);

        return DtoMapper.toResponse(eventoActualizado);
    }

    /**
     * Elimina un evento y revierte su impacto en el marcador y estadísticas.
     */
    @Override
    public void eliminarEvento(UUID eventoId) {
        EventoPartido evento = eventoPartidoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));

        Partido partido = evento.getPartido();
        Jugador jugador = evento.getJugador();

        boolean esLocal = jugador.getEquipo().getId().equals(partido.getEquipoLocal().getId());

        revertirEfectosEvento(partido, jugador, evento.getTipoEvento(), esLocal);

        eventoPartidoRepository.delete(evento);
    }

    /**
     * Modifica el marcador del partido si el evento es un gol
     * y actualiza las estadísticas del jugador.
     */
    private void aplicarEfectosEvento(
            Partido partido,
            Jugador jugador,
            TipoEvento tipoEvento,
            boolean esLocal) {

        if (esGol(tipoEvento)) {
            int golesLocal = partido.getGolesLocal();
            int golesVisitante = partido.getGolesVisitante();

            if (tipoEvento == TipoEvento.AUTOGOL) {
                if (esLocal) {
                    golesVisitante++;
                } else {
                    golesLocal++;
                }
            } else {
                if (esLocal) {
                    golesLocal++;
                } else {
                    golesVisitante++;
                }
            }

            partidoService.actualizarMarcador(partido.getId(), golesLocal, golesVisitante);
        }

        estadisticaJugadorService.actualizarEstadisticas(
                jugador,
                partido.getJornada().getTorneo(),
                jugador.getEquipo(),
                tipoEvento);
    }

    /**
     * Descuenta los goles del marcador y revierte las estadísticas del jugador.
     */
    private void revertirEfectosEvento(
            Partido partido,
            Jugador jugador,
            TipoEvento tipoEvento,
            boolean esLocal) {

        if (esGol(tipoEvento)) {
            int golesLocal = partido.getGolesLocal();
            int golesVisitante = partido.getGolesVisitante();

            if (tipoEvento == TipoEvento.AUTOGOL) {
                if (esLocal && golesVisitante > 0) {
                    golesVisitante--;
                } else if (!esLocal && golesLocal > 0) {
                    golesLocal--;
                }
            } else {
                if (esLocal && golesLocal > 0) {
                    golesLocal--;
                } else if (!esLocal && golesVisitante > 0) {
                    golesVisitante--;
                }
            }

            partidoService.actualizarMarcador(partido.getId(), golesLocal, golesVisitante);
        }

        estadisticaJugadorService.revertirEstadisticas(
                jugador,
                partido.getJornada().getTorneo(),
                jugador.getEquipo(),
                tipoEvento);
    }

    /**
     * Valida si el tipo de evento corresponde a un gol válido o autogol.
     */
    private boolean esGol(TipoEvento tipoEvento) {
        return tipoEvento == TipoEvento.GOL_JUGADA
                || tipoEvento == TipoEvento.GOL_CABEZA
                || tipoEvento == TipoEvento.GOL_PENAL
                || tipoEvento == TipoEvento.GOL_TIRO_LIBRE
                || tipoEvento == TipoEvento.AUTOGOL;
    }

    /**
     * Valida que el minuto del evento no sea negativo.
     */
    private void validarMinuto(int minuto) {
        if (minuto < 0) {
            throw new ValidacionException("El minuto no puede ser negativo");
        }
    }
}
