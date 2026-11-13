package com.lospaisas.dto.response;

import java.util.UUID;

import com.lospaisas.models.enums.Roles;

public record RolResponse(
    UUID id,
    Roles nombre) {
}
