package com.lospaisas.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipoRequest(

        @NotBlank @Size(min = 3, max = 50) 
        String nombre,

        @NotBlank @Size(min = 3, max = 80) 
        String entrenador,

        @NotNull 
        UUID torneoId) {
}
