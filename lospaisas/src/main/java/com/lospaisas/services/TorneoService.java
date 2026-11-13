package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.request.TorneoRequest;
import com.lospaisas.dto.response.TorneoResponse;

public interface TorneoService {

    TorneoResponse crearTorneo(TorneoRequest request);

    List<TorneoResponse> listarTorneos();

    TorneoResponse buscarPorId(UUID id);

    TorneoResponse actualizarTorneo(UUID id, TorneoRequest request);

    void eliminarTorneo(UUID id);

    List<TorneoResponse> listarTorneos(String buscar);
}
