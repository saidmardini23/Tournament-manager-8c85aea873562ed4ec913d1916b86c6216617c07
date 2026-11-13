package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.request.PartidoRequest;
import com.lospaisas.dto.response.PartidoResponse;
import com.lospaisas.models.enums.EstadoPartido;

public interface PartidoService {

    PartidoResponse crearPartido(PartidoRequest request);

    PartidoResponse buscarPorId(UUID id);

    List<PartidoResponse> listarPartidos();

    // El Controller desempaqueta CambiarEstadoRequest / MarcadorRequest y pasa los
    // valores.
    // Se dejan primitivos porque EventoPartidoServiceImpl también usa
    // actualizarMarcador internamente.
    PartidoResponse cambiarEstado(UUID partidoId, EstadoPartido estado);

    PartidoResponse actualizarMarcador(UUID partidoId, int golesLocal, int golesVisitante);

    void eliminarPartido(UUID id);

    List<PartidoResponse> listarPorTorneo(UUID torneoId);

    List<PartidoResponse> obtenerProximosPartidos(int limite);

    List<PartidoResponse> listarPorJornada(UUID jornadaId);

}


