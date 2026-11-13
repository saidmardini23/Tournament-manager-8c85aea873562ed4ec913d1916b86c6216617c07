package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.request.EquipoRequest;
import com.lospaisas.dto.response.EquipoResponse;

public interface EquipoService {

    EquipoResponse crearEquipo(EquipoRequest request);

    List<EquipoResponse> listarEquipos();

    List<EquipoResponse> listarEquipos(UUID torneoId);

    EquipoResponse buscarPorId(UUID id);

    EquipoResponse actualizarEquipo(UUID id, EquipoRequest request);

    void eliminarEquipo(UUID id);

}
