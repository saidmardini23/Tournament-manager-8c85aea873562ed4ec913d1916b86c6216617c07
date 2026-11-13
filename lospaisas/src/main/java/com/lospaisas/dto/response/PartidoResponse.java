package com.lospaisas.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.lospaisas.models.enums.EstadoPartido;

public record PartidoResponse(
        UUID id, 
        UUID jornadaId,
        UUID equipoLocalId, 
        String equipoLocalNombre,
        UUID equipoVisitanteId, 
        String equipoVisitanteNombre,
        UUID arbitroId, 
        String arbitroNombre,
        int golesLocal, 
        int golesVisitante,
        EstadoPartido estado, 
        LocalDateTime fecha) {
}
