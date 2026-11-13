package com.lospaisas.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TorneoRequest(

        @NotBlank @Size(min = 3, max = 100) 
        String nombre,

        @Size(max = 500) 
        String descripcion,

        @NotNull 
        LocalDate fechaInicio,
        LocalDate fechaFin,

        @Size(max = 30) 
        String estado,
        
        @Size(max = 50) 
        String formato) {

    @JsonIgnore
    @AssertTrue(message = "fechaFin no puede ser anterior a fechaInicio")
    public boolean isRangoFechasValido() {
        return fechaFin == null || fechaInicio == null || !fechaFin.isBefore(fechaInicio);
    }
}
