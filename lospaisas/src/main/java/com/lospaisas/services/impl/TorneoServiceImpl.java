package com.lospaisas.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.exceptions.RecursoNoEncontradoException;
import com.lospaisas.dto.DtoMapper;
import com.lospaisas.dto.request.TorneoRequest;
import com.lospaisas.dto.response.TorneoResponse;
import com.lospaisas.models.entity.Torneo;
import com.lospaisas.repositories.TorneoRepository;
import com.lospaisas.services.TorneoService;

import jakarta.transaction.Transactional;;

@Service
@Transactional
public class TorneoServiceImpl implements TorneoService {

    private final TorneoRepository torneoRepository;

    public TorneoServiceImpl(TorneoRepository torneoRepository) {
        this.torneoRepository = torneoRepository;
    }

    @Override
    public TorneoResponse crearTorneo(TorneoRequest request) {

        Torneo torneo = new Torneo();

        torneo.setNombre(request.nombre());
        torneo.setDescripcion(request.descripcion());
        torneo.setFechaInicio(request.fechaInicio());
        torneo.setFechaFin(request.fechaFin());
        torneo.setEstado(request.estado());
        torneo.setFormato(request.formato());

        return DtoMapper.toResponse(torneoRepository.save(torneo));
    }

    @Override
    public List<TorneoResponse> listarTorneos() {
        return torneoRepository.findAll().stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public TorneoResponse buscarPorId(UUID id) {
        return DtoMapper.toResponse(obtenerTorneo(id));
    }

    @Override
    public TorneoResponse actualizarTorneo(UUID id, TorneoRequest request) {

        Torneo torneo = obtenerTorneo(id);

        torneo.setNombre(request.nombre());
        torneo.setDescripcion(request.descripcion());
        torneo.setFechaInicio(request.fechaInicio());
        torneo.setFechaFin(request.fechaFin());
        torneo.setEstado(request.estado());
        torneo.setFormato(request.formato());

        return DtoMapper.toResponse(torneoRepository.save(torneo));
    }

    @Override
    public void eliminarTorneo(UUID id) {
        torneoRepository.delete(obtenerTorneo(id));
    }

    private Torneo obtenerTorneo(UUID id) {
        return torneoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado"));
    }

    @Override
    public List<TorneoResponse> listarTorneos(String buscar) {
        List<TorneoResponse> torneos = listarTorneos();
        if (buscar != null && !buscar.isBlank()) {
            String filtro = buscar.trim().toLowerCase();
            return torneos.stream()
                    .filter(t -> t.nombre() != null && t.nombre().toLowerCase().contains(filtro))
                    .toList();
        }
        return torneos;
    }
}
