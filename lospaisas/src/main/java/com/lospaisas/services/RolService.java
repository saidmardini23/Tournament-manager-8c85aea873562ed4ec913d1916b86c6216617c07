package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.request.RolRequest;
import com.lospaisas.dto.response.RolResponse;

public interface RolService {

    RolResponse crearRol(RolRequest request);

    List<RolResponse> listarRoles();

    RolResponse buscarPorId(UUID id);

    RolResponse actualizarRol(UUID id, RolRequest request);

    void eliminarRol(UUID id);
}
