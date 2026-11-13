package com.lospaisas.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record TorneoResponse(
        UUID id, 
        String nombre, 
        String descripcion,
        LocalDate fechaInicio, 
        LocalDate fechaFin,
        String estado,
        String formato) {
}
