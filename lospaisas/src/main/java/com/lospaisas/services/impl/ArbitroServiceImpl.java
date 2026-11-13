package com.lospaisas.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.exceptions.RecursoNoEncontradoException;
import com.lospaisas.dto.DtoMapper;
import com.lospaisas.dto.request.ArbitroRequest;
import com.lospaisas.dto.response.ArbitroResponse;
import com.lospaisas.models.entity.Arbitro;
import com.lospaisas.repositories.ArbitroRepository;
import com.lospaisas.services.ArbitroService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ArbitroServiceImpl implements ArbitroService {

    private final ArbitroRepository arbitroRepository;

    public ArbitroServiceImpl(ArbitroRepository arbitroRepository) {
        this.arbitroRepository = arbitroRepository;
    }

    @Override
    public ArbitroResponse crearArbitro(ArbitroRequest request) {

        Arbitro arbitro = new Arbitro();

        arbitro.setNombre(request.nombre());
        arbitro.setApellido(request.apellido());
        arbitro.setExperiencia(request.experiencia());
        arbitro.setLicencia(request.licencia());

        return DtoMapper.toResponse(arbitroRepository.save(arbitro));
    }

    @Override
    public List<ArbitroResponse> listarArbitros() {
        return arbitroRepository.findAll().stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public List<ArbitroResponse> listarArbitros(String buscar) {
        List<ArbitroResponse> arbitros = listarArbitros();
        if (buscar != null && !buscar.isBlank()) {
            String termino = buscar.trim().toLowerCase();
            return arbitros.stream()
                    .filter(a -> (a.nombre() != null && a.nombre().toLowerCase().contains(termino))
                            || (a.apellido() != null && a.apellido().toLowerCase().contains(termino)))
                    .toList();
        }
        return arbitros;
    }

    @Override
    public ArbitroResponse buscarPorId(UUID id) {
        return DtoMapper.toResponse(obtenerArbitro(id));
    }

    @Override
    public ArbitroResponse actualizarArbitro(
            UUID id,
            ArbitroRequest request) {

        Arbitro arbitro = obtenerArbitro(id);

        arbitro.setNombre(request.nombre());
        arbitro.setApellido(request.apellido());
        arbitro.setExperiencia(request.experiencia());
        arbitro.setLicencia(request.licencia());

        return DtoMapper.toResponse(arbitroRepository.save(arbitro));
    }

    @Override
    public void eliminarArbitro(UUID id) {
        arbitroRepository.delete(obtenerArbitro(id));
    }

    private Arbitro obtenerArbitro(UUID id) {
        return arbitroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Árbitro no encontrado"));
    }
}