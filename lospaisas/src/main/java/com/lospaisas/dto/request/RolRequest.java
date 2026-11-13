package com.lospaisas.dto.request;

import com.lospaisas.models.enums.Roles;

import jakarta.validation.constraints.NotNull;

public record RolRequest(
    
    @NotNull 
    Roles nombre) {
}
