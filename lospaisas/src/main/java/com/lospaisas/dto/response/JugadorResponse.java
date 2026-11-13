
package com.lospaisas.dto.response;

import java.util.UUID;

import com.lospaisas.models.enums.Posiciones;

public record JugadorResponse(
        UUID id,
        String nombre,
        String apellido,
        UUID equipoId,
        String equipoNombre,
        Posiciones posicion) {
}
