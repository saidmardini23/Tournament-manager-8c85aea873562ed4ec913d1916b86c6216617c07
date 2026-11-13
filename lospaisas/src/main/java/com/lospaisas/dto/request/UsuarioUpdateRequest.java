package com.lospaisas.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Edición: password opcional (si viene null no se cambia). */
public record UsuarioUpdateRequest(

        @NotBlank @Size(min = 3, max = 15) 
        String nombre,

        @NotBlank @Email @Size(max = 50) 
        String email,

        @Size(min = 8, max = 72) 
        String password,
        
        @NotNull 
        UUID rolId) {
}
