
package com.lospaisas.dto.request;

import java.util.UUID;

import com.lospaisas.models.enums.Posiciones;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JugadorRequest(

                @NotBlank String nombre,

                @NotBlank String apellido,

                @NotNull UUID equipoId,

                @NotNull Posiciones posicion

) {
}
