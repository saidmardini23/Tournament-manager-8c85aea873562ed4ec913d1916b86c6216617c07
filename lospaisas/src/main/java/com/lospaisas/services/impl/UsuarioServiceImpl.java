package com.lospaisas.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lospaisas.exceptions.RecursoNoEncontradoException;
import com.lospaisas.dto.DtoMapper;
import com.lospaisas.dto.request.UsuarioRequest;
import com.lospaisas.dto.request.UsuarioUpdateRequest;
import com.lospaisas.dto.response.UsuarioResponse;
import com.lospaisas.models.entity.Rol;
import com.lospaisas.models.entity.Usuario;
import com.lospaisas.repositories.RolRepository;
import com.lospaisas.repositories.UsuarioRepository;
import com.lospaisas.services.UsuarioService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public UsuarioResponse crearUsuario(UsuarioRequest request) {

        Usuario usuario = new Usuario();

        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setPasswordHash(request.password());
        usuario.setRol(obtenerRol(request.rolId()));

        return DtoMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(DtoMapper::toResponse)
                .toList();
    }

    @Override
    public UsuarioResponse buscarPorId(UUID id) {
        return DtoMapper.toResponse(obtenerUsuario(id));
    }

    @Override
    public UsuarioResponse buscarPorEmail(String email) {
        return DtoMapper.toResponse(obtenerEntidadPorEmail(email));
    }

    @Override
    public UsuarioResponse actualizarUsuario(
            UUID id,
            UsuarioUpdateRequest request) {

        Usuario usuario = obtenerUsuario(id);

        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        if (request.password() != null) {
            usuario.setPasswordHash(request.password());
        }
        usuario.setRol(obtenerRol(request.rolId()));

        return DtoMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public void eliminarUsuario(UUID id) {
        usuarioRepository.delete(obtenerUsuario(id));
    }

    @Override
    public Usuario obtenerEntidadPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private Usuario obtenerUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private Rol obtenerRol(UUID id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));
    }
}