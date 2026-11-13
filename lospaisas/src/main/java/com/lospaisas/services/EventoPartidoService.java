package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.request.EventoPartidoRequest;
import com.lospaisas.dto.request.EventoPartidoUpdateRequest;
import com.lospaisas.dto.response.EventoPartidoResponse;

public interface EventoPartidoService {

    EventoPartidoResponse registrarEvento(EventoPartidoRequest request);

    List<EventoPartidoResponse> listarPorPartido(UUID partidoId);

    EventoPartidoResponse actualizarEvento(UUID eventoId, EventoPartidoUpdateRequest request);

    void eliminarEvento(UUID eventoId);
}
