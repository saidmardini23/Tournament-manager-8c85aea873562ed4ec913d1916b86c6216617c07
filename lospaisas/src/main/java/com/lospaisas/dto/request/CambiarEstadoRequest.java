package com.lospaisas.dto.request;

import com.lospaisas.models.enums.EstadoPartido;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(
    
    @NotNull 
    EstadoPartido estado) {
}
