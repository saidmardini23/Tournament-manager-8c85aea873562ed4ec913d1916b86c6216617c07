package com.lospaisas.services;

import java.util.List;
import java.util.UUID;

import com.lospaisas.dto.response.TablaPosicionResponse;
import com.lospaisas.models.entity.Equipo;
import com.lospaisas.models.entity.Partido;
import com.lospaisas.models.entity.Torneo;

public interface TablaPosicionService {

    void actualizarTabla(Partido partido);

    void inicializarPosicion(Torneo torneo, Equipo equipo);

    void eliminarPosicion(Equipo equipo);

    List<TablaPosicionResponse> obtenerTabla(UUID torneoId);

    List<TablaPosicionResponse> obtenerTablaCompleta(UUID torneoId, UUID equipoId);
}
