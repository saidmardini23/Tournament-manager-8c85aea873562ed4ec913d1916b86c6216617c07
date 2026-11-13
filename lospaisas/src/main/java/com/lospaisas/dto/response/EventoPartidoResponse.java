package com.lospaisas.dto.response;

import java.util.UUID;

import com.lospaisas.models.enums.TipoEvento;

public record EventoPartidoResponse(
        UUID id, 
        UUID partidoId,
        UUID jugadorId, 
        String jugadorNombre,
        TipoEvento tipoEvento, 
        int minuto, 
        String descripcion) {
}
