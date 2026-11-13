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
@Table(name = "estadisticas_jugadores")
@Getter
@Setter
public class EstadisticaJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_torneo")
    private Torneo torneo;

    @ManyToOne
    @JoinColumn(name = "id_jugador")
    private Jugador jugador;

    @ManyToOne
    @JoinColumn(name = "id_equipo")
    private Equipo equipo;

    @Column
    private int goles;

    @Column
    private int asistencias;

    @Column
    private int tarjetasAmarillas;

    @Column
    private int tarjetasRojas;

    @Column
    private int partidosJugados;

    public EstadisticaJugador() {
    }

    public EstadisticaJugador(UUID id, Torneo torneo, Jugador jugador, Equipo equipo, int goles, int asistencias,
            int tarjetasAmarillas, int tarjetasRojas, int partidosJugados) {
        this.id = id;
        this.torneo = torneo;
        this.jugador = jugador;
        this.equipo = equipo;
        this.goles = goles;
        this.asistencias = asistencias;
        this.tarjetasAmarillas = tarjetasAmarillas;
        this.tarjetasRojas = tarjetasRojas;
        this.partidosJugados = partidosJugados;
    }
}