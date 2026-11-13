package com.lospaisas.models.entity;

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
@Table(name = "arbitros")
@Getter
@Setter
public class Arbitro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column
    private int experiencia;

    @Column
    private String licencia;

    public Arbitro() {
    }

    public Arbitro(UUID id, String nombre, String apellido, int experiencia, String licencia) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.experiencia = experiencia;
        this.licencia = licencia;
    }
}