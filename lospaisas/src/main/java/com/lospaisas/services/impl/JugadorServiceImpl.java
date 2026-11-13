
package com.lospaisas.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.exceptions.RecursoNoEncontradoException;
import com.lospaisas.dto.DtoMapper;
import com.lospaisas.dto.request.JugadorRequest;
import com.lospaisas.dto.response.JugadorResponse;
import com.lospaisas.models.entity.Equipo;
import com.lospaisas.models.entity.Jugador;
import com.lospaisas.repositories.EquipoRepository;
import com.lospaisas.repositories.JugadorRepository;
import com.lospaisas.services.JugadorService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class JugadorServiceImpl implements JugadorService {

    private final JugadorRepository jugadorRepository;
    private final EquipoRepository equipoRepository;

    public JugadorServiceImpl(
            JugadorRepository jugadorRepository,
            EquipoRepository equipoRepository) {
        this.jugadorRepository = jugadorRepository;
        this.equipoRepository = equipoRepository;
    }

    @Override
    public JugadorResponse crearJugador(JugadorRequest request) {

        Jugador jugador = new Jugador();

        jugador.setNombre(request.nombre());
        jugador.setApellido(request.apellido());
        jugador.setEquipo(obtenerEquipo(request.equipoId()));
        jugador.setPosicion(request.posicion());

        return DtoMapper.toResponse(jugadorRepository.save(jugador));
    }

    @Override
    public List<JugadorResponse> listarJugadores() {
        return jugadorRepository.findAll().stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public JugadorResponse buscarPorId(UUID id) {
        return DtoMapper.toResponse(obtenerJugador(id));
    }

    @Override
    public JugadorResponse actualizarJugador(UUID id, JugadorRequest request) {

        Jugador jugador = obtenerJugador(id);

        jugador.setNombre(request.nombre());
        jugador.setApellido(request.apellido());
        jugador.setEquipo(obtenerEquipo(request.equipoId()));
        jugador.setPosicion(request.posicion());

        return DtoMapper.toResponse(jugadorRepository.save(jugador));
    }

    @Override
    public void eliminarJugador(UUID id) {
        jugadorRepository.delete(obtenerJugador(id));
    }

    @Override
    public List<JugadorResponse> buscarPorEquipo(UUID equipoId) {
        return jugadorRepository.findByEquipoId(equipoId).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    private Jugador obtenerJugador(UUID id) {
        return jugadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Jugador no encontrado"));
    }

    private Equipo obtenerEquipo(UUID id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Equipo no encontrado"));
    }

    @Override
    public List<JugadorResponse> buscarPorEquipo(UUID equipoId, String buscar) {
        List<JugadorResponse> jugadores = buscarPorEquipo(equipoId);
        if (buscar != null && !buscar.isBlank()) {
            String termino = buscar.trim().toLowerCase();
            return jugadores.stream()
                    .filter(j -> (j.nombre() != null && j.nombre().toLowerCase().contains(termino))
                            || (j.apellido() != null && j.apellido().toLowerCase().contains(termino)))
                    .toList();
        }
        return jugadores;
    }
}
