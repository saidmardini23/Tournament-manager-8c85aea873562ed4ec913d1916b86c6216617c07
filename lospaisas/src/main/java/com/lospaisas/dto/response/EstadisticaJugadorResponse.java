package com.lospaisas.dto.response;

import java.util.UUID;

public record EstadisticaJugadorResponse(
        UUID id, 
        UUID jugadorId,
         String jugadorNombre,
        UUID equipoId, 
        String equipoNombre,
        int goles, 
        int asistencias,
        int tarjetasAmarillas, 
        int tarjetasRojas, 
        int partidosJugados) {
}
