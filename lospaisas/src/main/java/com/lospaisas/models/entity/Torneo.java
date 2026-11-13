package com.lospaisas.models.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "torneos")
@Getter
@Setter
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @Column
    private LocalDate fechaInicio;

    @Column
    private LocalDate fechaFin;

    @Column
    private String estado;

    @Column
    private String formato;

    public Torneo() {
    }

    public Torneo(UUID id, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin, String estado, String formato) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.formato = formato;
    }

}