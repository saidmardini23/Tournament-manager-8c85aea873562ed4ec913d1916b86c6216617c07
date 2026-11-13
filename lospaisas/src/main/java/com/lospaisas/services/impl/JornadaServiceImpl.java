package com.lospaisas.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.exceptions.RecursoNoEncontradoException;
import com.lospaisas.dto.DtoMapper;
import com.lospaisas.dto.request.JornadaRequest;
import com.lospaisas.dto.response.JornadaFixtureResponse;
import com.lospaisas.dto.response.JornadaResponse;
import com.lospaisas.dto.response.PartidoResponse;
import com.lospaisas.models.entity.Jornada;
import com.lospaisas.models.entity.Torneo;
import com.lospaisas.repositories.JornadaRepository;
import com.lospaisas.repositories.TorneoRepository;
import com.lospaisas.services.JornadaService;
import com.lospaisas.services.PartidoService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class JornadaServiceImpl implements JornadaService {

    private final JornadaRepository jornadaRepository;
    private final TorneoRepository torneoRepository;
    private final PartidoService partidoService;

    public JornadaServiceImpl(
            JornadaRepository jornadaRepository,
            TorneoRepository torneoRepository,
            PartidoService partidoService) {
        this.jornadaRepository = jornadaRepository;
        this.torneoRepository = torneoRepository;
        this.partidoService = partidoService;
    }

    @Override
    public JornadaResponse crearJornada(JornadaRequest request) {

        Torneo torneo = torneoRepository.findById(request.torneoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado"));

        Jornada jornada = new Jornada();

        jornada.setTorneo(torneo);
        jornada.setNumeroJornada(request.numeroJornada());

        return DtoMapper.toResponse(jornadaRepository.save(jornada));
    }

    @Override
    public List<JornadaResponse> listarJornadas() {
        return jornadaRepository.findAll().stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public List<JornadaResponse> listarPorTorneo(UUID torneoId) {
        return jornadaRepository.findByTorneoId(torneoId).stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public JornadaResponse buscarPorId(UUID id) {
        return DtoMapper.toResponse(jornadaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Jornada no encontrada")));
    }

    @Override
    public void eliminarJornada(UUID id) {
        jornadaRepository.deleteById(id);
    }

    @Override
    public List<JornadaFixtureResponse> obtenerFixture(UUID torneoId, String estado, UUID equipoId) {
        List<JornadaResponse> jornadas = listarPorTorneo(torneoId);

        // 3. Usar el método filtrado por torneo en lugar de traer toda la BD
        List<PartidoResponse> partidosTorneo = partidoService.listarPorTorneo(torneoId);

        List<JornadaFixtureResponse> fixture = new ArrayList<>();

        for (JornadaResponse jornada : jornadas) {
            List<PartidoResponse> partidosJornada = partidosTorneo.stream()
                    .filter(p -> jornada.id().equals(p.jornadaId()))
                    .filter(p -> {
                        if (estado != null && !estado.isBlank() && !"TODOS".equalsIgnoreCase(estado.trim())) {
                            return p.estado() != null && p.estado().name().equalsIgnoreCase(estado.trim());
                        }
                        return true;
                    })
                    .filter(p -> {
                        if (equipoId != null) {
                            return equipoId.equals(p.equipoLocalId()) || equipoId.equals(p.equipoVisitanteId());
                        }
                        return true;
                    })
                    .toList();

            fixture.add(new JornadaFixtureResponse(jornada, partidosJornada));
        }

        return fixture;
    }

    @Override
        public JornadaResponse actualizarJornada(UUID id, JornadaRequest request) {
            Jornada jornada = jornadaRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Jornada no encontrada"));
    
            jornada.setNumeroJornada(request.numeroJornada());
    
            if (request.torneoId() != null) {
                Torneo torneo = torneoRepository.findById(request.torneoId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado"));
                jornada.setTorneo(torneo);
            }
    
            return DtoMapper.toResponse(jornadaRepository.save(jornada));
        }
}
