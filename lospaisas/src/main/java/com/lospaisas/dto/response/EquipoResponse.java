package com.lospaisas.dto.response;

import java.util.UUID;

public record EquipoResponse(
        UUID id, 
        String nombre, 
        String entrenador,
        UUID torneoId, 
        String torneoNombre) {
}
