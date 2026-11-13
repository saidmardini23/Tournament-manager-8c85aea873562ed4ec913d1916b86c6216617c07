package com.lospaisas.dto.response;

import java.util.UUID;

public record TablaPosicionResponse(
        UUID equipoId, 
        String equipoNombre,
        int puntos, 
        int partidosJugados,
        int partidosGanados, 
        int partidosEmpatados,
        int partidosPerdidos,
        int golesFavor, 
        int golesContra, 
        int diferenciaGoles) {
}
