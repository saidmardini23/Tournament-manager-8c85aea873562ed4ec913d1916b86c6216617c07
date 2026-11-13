package com.lospaisas.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArbitroRequest(
        
        @NotBlank @Size(min = 2, max = 50) 
        String nombre,

        @NotBlank @Size(min = 2, max = 50) 
        String apellido,

        @Min(0) @Max(60)
         int experiencia,

        @Size(max = 30) 
        String licencia) {
}
