package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.request.JornadaRequest;
import com.lospaisas.dto.response.JornadaFixtureResponse;
import com.lospaisas.dto.response.JornadaResponse;

public interface JornadaService {

    JornadaResponse crearJornada(JornadaRequest request);

    List<JornadaResponse> listarJornadas();

    List<JornadaResponse> listarPorTorneo(UUID torneoId);

    JornadaResponse buscarPorId(UUID id);

    void eliminarJornada(UUID id);

    List<JornadaFixtureResponse> obtenerFixture(UUID torneoId, String estado, UUID equipoId);

     // Método agregado para actualizar la jornada
        JornadaResponse actualizarJornada(UUID id, JornadaRequest request);
}
