package com.lospaisas.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

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
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fecha) {
}