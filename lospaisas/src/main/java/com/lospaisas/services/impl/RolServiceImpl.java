package com.lospaisas.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.dto.DtoMapper;
import com.lospaisas.exceptions.RecursoNoEncontradoException;
import com.lospaisas.dto.request.RolRequest;
import com.lospaisas.dto.response.RolResponse;
import com.lospaisas.models.entity.Rol;
import com.lospaisas.repositories.RolRepository;
import com.lospaisas.services.RolService;

import jakarta.transaction.Transactional;
@Service
@Transactional
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    
    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public RolResponse crearRol(RolRequest request) {

        Rol rol = new Rol();

        rol.setNombre(request.nombre());

        return DtoMapper.toResponse(rolRepository.save(rol));
    }

    @Override
    public List<RolResponse> listarRoles() {
        return rolRepository.findAll().stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public RolResponse buscarPorId(UUID id) {
        return DtoMapper.toResponse(obtenerRol(id));
    }

    @Override
    public RolResponse actualizarRol(
            UUID id,
            RolRequest request) {

        Rol rol = obtenerRol(id);

        rol.setNombre(request.nombre());

        return DtoMapper.toResponse(rolRepository.save(rol));
    }

    @Override
    public void eliminarRol(UUID id) {
        rolRepository.delete(obtenerRol(id));
    }

    private Rol obtenerRol(UUID id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));
    }
}
