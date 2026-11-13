package com.lospaisas.models.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.lospaisas.models.enums.EstadoPartido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "partidos")
@Getter
@Setter
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_jornada")
    private Jornada jornada;

    @ManyToOne
    @JoinColumn(name = "id_equipo_local")
    private Equipo equipoLocal;

    @ManyToOne
    @JoinColumn(name = "id_equipo_visitante")
    private Equipo equipoVisitante;

    @ManyToOne
    @JoinColumn(name = "id_arbitro")
    private Arbitro arbitro;

    private int golesLocal;

    private int golesVisitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPartido estado;

    private LocalDateTime fecha;

    public Partido() {
    }

    public Partido(UUID id, Jornada jornada, Equipo equipoLocal, Equipo equipoVisitante, Arbitro arbitro, int golesLocal, int golesVisitante, EstadoPartido estado, LocalDateTime fecha) {
        this.id = id;
        this.jornada = jornada;
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.arbitro = arbitro;
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
        this.estado = estado;
        this.fecha = fecha;
    }

}
