package com.lospaisas.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record PartidoRequest(

        @NotNull 
        UUID jornadaId,

        @NotNull 
        UUID equipoLocalId,

        @NotNull
         UUID equipoVisitanteId,

        @NotNull 
        UUID arbitroId,

        @NotNull 
        LocalDateTime fecha) {
}
