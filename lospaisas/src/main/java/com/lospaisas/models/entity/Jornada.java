package com.lospaisas.models.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "jornadas")
@Getter
@Setter
public class Jornada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_torneo")
    private Torneo torneo;

    @Column
    private int numeroJornada;

    public Jornada() {
    }

    public Jornada(UUID id, Torneo torneo, int numeroJornada) {
        this.id = id;
        this.torneo = torneo;
        this.numeroJornada = numeroJornada;
    }
    
}