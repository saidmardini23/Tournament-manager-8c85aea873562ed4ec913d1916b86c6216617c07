package com.lospaisas.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.dto.DtoMapper;
import com.lospaisas.exceptions.RecursoNoEncontradoException;
import com.lospaisas.exceptions.ValidacionException;
import com.lospaisas.dto.request.PartidoRequest;
import com.lospaisas.dto.response.PartidoResponse;
import com.lospaisas.models.entity.Arbitro;
import com.lospaisas.models.entity.Equipo;
import com.lospaisas.models.entity.Jornada;
import com.lospaisas.models.entity.Partido;
import com.lospaisas.models.enums.EstadoPartido;
import com.lospaisas.repositories.ArbitroRepository;
import com.lospaisas.repositories.EquipoRepository;
import com.lospaisas.repositories.JornadaRepository;
import com.lospaisas.repositories.PartidoRepository;
import com.lospaisas.services.EstadisticaJugadorService;
import com.lospaisas.services.PartidoService;
import com.lospaisas.services.TablaPosicionService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PartidoServiceImpl implements PartidoService {

    private final PartidoRepository partidoRepository;
    private final JornadaRepository jornadaRepository;
    private final EquipoRepository equipoRepository;
    private final ArbitroRepository arbitroRepository;
    private final TablaPosicionService tablaPosicionService;
    private final EstadisticaJugadorService estadisticaJugadorService;

    public PartidoServiceImpl(
            PartidoRepository partidoRepository,
            JornadaRepository jornadaRepository,
            EquipoRepository equipoRepository,
            ArbitroRepository arbitroRepository,
            TablaPosicionService tablaPosicionService,
            EstadisticaJugadorService estadisticaJugadorService) {
        this.partidoRepository = partidoRepository;
        this.jornadaRepository = jornadaRepository;
        this.equipoRepository = equipoRepository;
        this.arbitroRepository = arbitroRepository;
        this.tablaPosicionService = tablaPosicionService;
        this.estadisticaJugadorService = estadisticaJugadorService;
    }

    /**
     * Crea y programa un nuevo partido validando las entidades asociadas y la
     * coherencia de los equipos.
     */
    @Override
    public PartidoResponse crearPartido(PartidoRequest request) {

        Jornada jornada = jornadaRepository.findById(request.jornadaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Jornada no encontrada"));

        Equipo equipoLocal = equipoRepository.findById(request.equipoLocalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo local no encontrado"));

        Equipo equipoVisitante = equipoRepository.findById(request.equipoVisitanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo visitante no encontrado"));

        Arbitro arbitro = arbitroRepository.findById(request.arbitroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Árbitro no encontrado"));

        if (equipoLocal.getId().equals(equipoVisitante.getId())) {
            throw new ValidacionException("El equipo local y visitante no pueden ser el mismo");
        }

        Partido partido = new Partido();
        partido.setJornada(jornada);
        partido.setEquipoLocal(equipoLocal);
        partido.setEquipoVisitante(equipoVisitante);
        partido.setArbitro(arbitro);
        partido.setGolesLocal(0);
        partido.setGolesVisitante(0);
        partido.setEstado(EstadoPartido.PROGRAMADO);
        partido.setFecha(request.fecha());

        return DtoMapper.toResponse(partidoRepository.save(partido));
    }

    /**
     * Busca un partido por su identificador único.
     */
    @Override
    public PartidoResponse buscarPorId(UUID id) {
        return DtoMapper.toResponse(obtenerPartido(id));
    }

    /**
     * Recupera el listado completo de partidos registrados.
     */
    @Override
    public List<PartidoResponse> listarPartidos() {
        return partidoRepository.findAll().stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    /**
     * Actualiza el estado del partido. Si el estado cambia a FINALIZADO por primera
     * vez,
     * procesa la tabla de posiciones y registra los partidos jugados para los
     * plantilla.
     */
    @Override
    public PartidoResponse cambiarEstado(
            UUID partidoId,
            EstadoPartido estado) {

        Partido partido = obtenerPartido(partidoId);
        EstadoPartido estadoAnterior = partido.getEstado();

        partido.setEstado(estado);
        Partido partidoGuardado = partidoRepository.save(partido);

        if (estado == EstadoPartido.FINALIZADO && estadoAnterior != EstadoPartido.FINALIZADO) {
            tablaPosicionService.actualizarTabla(partidoGuardado);
            estadisticaJugadorService.registrarPartidoJugado(partidoGuardado);
        }

        return DtoMapper.toResponse(partidoGuardado);
    }

    /**
     * Actualiza la cantidad de goles anotados por los equipos local y visitante.
     */
    @Override
    public PartidoResponse actualizarMarcador(
            UUID partidoId,
            int golesLocal,
            int golesVisitante) {

        Partido partido = obtenerPartido(partidoId);

        if (golesLocal < 0 || golesVisitante < 0) {
            throw new ValidacionException("Los goles no pueden ser negativos");
        }

        partido.setGolesLocal(golesLocal);
        partido.setGolesVisitante(golesVisitante);

        return DtoMapper.toResponse(partidoRepository.save(partido));
    }

    /**
     * Elimina un partido de la base de datos según su identificador.
     */
    @Override
    public void eliminarPartido(UUID id) {
        Partido partido = obtenerPartido(id);
        partidoRepository.delete(partido);
    }

    private Partido obtenerPartido(UUID id) {
        return partidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado"));
    }

    @Override
    public List<PartidoResponse> listarPorTorneo(UUID torneoId) {
        return partidoRepository.findByJornadaTorneoId(torneoId).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public List<PartidoResponse> obtenerProximosPartidos(int limite) {
        return partidoRepository.findTop5ByEstadoOrderByFechaAsc(EstadoPartido.PROGRAMADO).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

@Override
public List<PartidoResponse> listarPorJornada(UUID jornadaId) {
    return partidoRepository.findByJornadaId(jornadaId).stream()
            .map(DtoMapper::toResponse)
            .toList();
}
}
