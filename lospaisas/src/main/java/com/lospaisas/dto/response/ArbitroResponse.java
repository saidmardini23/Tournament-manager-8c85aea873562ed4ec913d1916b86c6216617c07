package com.lospaisas.dto.response;

import java.util.UUID;

public record ArbitroResponse(
    UUID id, 
    String nombre, 
    String apellido, 
    int experiencia, 
    String licencia) {
}
