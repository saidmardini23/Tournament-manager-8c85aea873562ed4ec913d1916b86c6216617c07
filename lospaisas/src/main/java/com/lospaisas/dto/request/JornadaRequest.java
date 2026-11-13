package com.lospaisas.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record JornadaRequest(
        
        @NotNull 
        UUID torneoId,

        @Min(1) 
        int numeroJornada) {
}
