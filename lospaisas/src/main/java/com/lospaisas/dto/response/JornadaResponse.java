package com.lospaisas.dto.response;

import java.util.UUID;

public record JornadaResponse(
    UUID id, 
    UUID torneoId,
     int numeroJornada) {
}
