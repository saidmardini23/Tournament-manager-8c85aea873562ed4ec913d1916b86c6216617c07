package com.lospaisas.dto.request;

import com.lospaisas.models.enums.TipoEvento;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EventoPartidoUpdateRequest(

        @NotNull 
        TipoEvento tipoEvento,

        @Min(0) @Max(130) 
        int minuto) {
}
