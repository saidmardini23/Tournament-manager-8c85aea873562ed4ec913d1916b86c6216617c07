package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.request.UsuarioRequest;
import com.lospaisas.dto.request.UsuarioUpdateRequest;
import com.lospaisas.dto.response.UsuarioResponse;
import com.lospaisas.models.entity.Usuario;

public interface UsuarioService {

    UsuarioResponse crearUsuario(UsuarioRequest request);

    List<UsuarioResponse> listarUsuarios();

    UsuarioResponse buscarPorId(UUID id);

    UsuarioResponse buscarPorEmail(String email);

    UsuarioResponse actualizarUsuario(UUID id, UsuarioUpdateRequest request);

    void eliminarUsuario(UUID id);

    /**
     * SOLO USO INTERNO (autenticación ).
     * Devuelve la entidad con passwordHash; nunca debe llegar a un Controller.
     */
    Usuario obtenerEntidadPorEmail(String email);
}
