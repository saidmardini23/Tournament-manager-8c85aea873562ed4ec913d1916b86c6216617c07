package com.lospaisas.dto.response;

import java.util.UUID;

import com.lospaisas.models.enums.Roles;

import jakarta.validation.constraints.NotNull;

/** Sin passwordHash, a propósito. */
public record UsuarioResponse(
    UUID id, 
    String nombre, 
    String email, 
    Roles rol) {

    public @NotNull UUID rolId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rolId'");
    }
}
