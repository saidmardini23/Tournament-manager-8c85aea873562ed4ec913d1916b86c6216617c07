package com.lospaisas.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MarcadorRequest(

        @Min(0) @Max(99) 
        int golesLocal,

        @Min(0) @Max(99) 
        int golesVisitante) {
}
