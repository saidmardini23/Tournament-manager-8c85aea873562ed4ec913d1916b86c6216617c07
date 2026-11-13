package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.request.JugadorRequest;
import com.lospaisas.dto.response.JugadorResponse;

public interface JugadorService {

    JugadorResponse crearJugador(JugadorRequest request);

    List<JugadorResponse> listarJugadores();

    JugadorResponse buscarPorId(UUID id);

    JugadorResponse actualizarJugador(UUID id, JugadorRequest request);

    void eliminarJugador(UUID id);

    List<JugadorResponse> buscarPorEquipo(UUID equipoId);

    List<JugadorResponse> buscarPorEquipo(UUID equipoId, String buscar);
}
