package com.lospaisas.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.dto.DtoMapper;
import com.lospaisas.dto.request.EquipoRequest;
import com.lospaisas.dto.response.EquipoResponse;
import com.lospaisas.exceptions.RecursoNoEncontradoException;
import com.lospaisas.exceptions.ValidacionException;
import com.lospaisas.models.entity.Equipo;
import com.lospaisas.models.entity.Torneo;
import com.lospaisas.repositories.EquipoRepository;
import com.lospaisas.repositories.PartidoRepository;
import com.lospaisas.repositories.TorneoRepository;
import com.lospaisas.services.EquipoService;
import com.lospaisas.services.TablaPosicionService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EquipoServiceImpl implements EquipoService {

    private final EquipoRepository equipoRepository;
    private final TorneoRepository torneoRepository;
    private final PartidoRepository partidoRepository;
    private final TablaPosicionService tablaPosicionService;

    public EquipoServiceImpl(
            EquipoRepository equipoRepository,
            TorneoRepository torneoRepository,
            PartidoRepository partidoRepository,
            TablaPosicionService tablaPosicionService) {
        this.equipoRepository = equipoRepository;
        this.torneoRepository = torneoRepository;
        this.partidoRepository = partidoRepository;
        this.tablaPosicionService = tablaPosicionService;
    }

    @Override
    public EquipoResponse crearEquipo(EquipoRequest request) {

        Torneo torneo = obtenerTorneo(request.torneoId());

        Equipo equipo = new Equipo();

        equipo.setNombre(request.nombre());
        equipo.setEntrenador(request.entrenador());
        equipo.setTorneo(torneo);

        Equipo guardado = equipoRepository.save(equipo);
        tablaPosicionService.inicializarPosicion(torneo, guardado);

        return DtoMapper.toResponse(guardado);
    }

    @Override
    public List<EquipoResponse> listarEquipos() {
        return equipoRepository.findAll().stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public List<EquipoResponse> listarEquipos(UUID torneoId) {
        if (torneoId == null) {
            return listarEquipos();
        }
        return equipoRepository.findByTorneoId(torneoId).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public EquipoResponse buscarPorId(UUID id) {
        return DtoMapper.toResponse(obtenerEquipo(id));
    }

    @Override
    public EquipoResponse actualizarEquipo(UUID id, EquipoRequest request) {

        Equipo equipo = obtenerEquipo(id);

        if (!equipo.getTorneo().getId().equals(request.torneoId())) {
            throw new ValidacionException("No se puede cambiar el torneo de un equipo ya registrado");
        }

        equipo.setNombre(request.nombre());
        equipo.setEntrenador(request.entrenador());

        return DtoMapper.toResponse(equipoRepository.save(equipo));
    }

    @Override
    public void eliminarEquipo(UUID id) {

        Equipo equipo = obtenerEquipo(id);

        if (partidoRepository.existsByEquipoLocalIdOrEquipoVisitanteId(id, id)) {
            throw new ValidacionException("No se puede eliminar un equipo que tiene partidos registrados");
        }

        tablaPosicionService.eliminarPosicion(equipo);
        equipoRepository.delete(equipo);
    }

    private Equipo obtenerEquipo(UUID id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado"));
    }

    private Torneo obtenerTorneo(UUID id) {
        return torneoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado"));
    }
}
