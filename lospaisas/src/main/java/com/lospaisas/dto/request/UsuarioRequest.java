package com.lospaisas.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Alta de usuario. La password llega en claro y el Service la hashea; nunca se guarda ni se devuelve tal cual. */
public record UsuarioRequest(

        @NotBlank @Size(min = 3, max = 50) 
        String nombre,

        @NotBlank @Email @Size(max = 100) 
        String email,

        @NotBlank @Size(min = 8, max = 72) 
        String password, 

        @NotNull 
        UUID rolId) {
}
