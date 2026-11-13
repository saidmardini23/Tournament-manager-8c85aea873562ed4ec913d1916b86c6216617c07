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
@Table(name = "tabla_posiciones")
@Getter
@Setter
public class TablaPosicion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_torneo")
    private Torneo torneo;

    @ManyToOne
    @JoinColumn(name = "id_equipo")
    private Equipo equipo;

    @Column
    private int puntos;

    @Column
    private int partidosJugados;

    @Column
    private int partidosGanados;

    @Column
    private int partidosEmpatados;

    @Column
    private int partidosPerdidos;

    @Column
    private int golesFavor;

    @Column
    private int golesContra;

    @Column
    private int diferenciaGoles;

    public TablaPosicion() {
    }

    public TablaPosicion(UUID id, Torneo torneo, Equipo equipo, int puntos, int partidosJugados, int partidosGanados, int partidosEmpatados, int partidosPerdidos, int golesFavor, int golesContra, int diferenciaGoles) {
        this.id = id;
        this.torneo = torneo;
        this.equipo = equipo;
        this.puntos = puntos;
        this.partidosJugados = partidosJugados;
        this.partidosGanados = partidosGanados;
        this.partidosEmpatados = partidosEmpatados;
        this.partidosPerdidos = partidosPerdidos;
        this.golesFavor = golesFavor;
        this.golesContra = golesContra;
        this.diferenciaGoles = diferenciaGoles;
    }

}