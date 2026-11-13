package com.lospaisas.models.entity;

import java.util.UUID;

import com.lospaisas.models.enums.TipoEvento;

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
@Table(name = "eventos_partido")
@Getter
@Setter
public class EventoPartido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_partido")
    private Partido partido;

    @ManyToOne
    @JoinColumn(name = "id_jugador")
    private Jugador jugador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEvento tipoEvento;

    @Column
    private int minuto;

    @Column
    private String descripcion;

    public EventoPartido() {
    }

    public EventoPartido(UUID id, Partido partido, Jugador jugador, TipoEvento tipoEvento, int minuto,
            String descripcion) {
        this.id = id;
        this.partido = partido;
        this.jugador = jugador;
        this.tipoEvento = tipoEvento;
        this.minuto = minuto;
        this.descripcion = descripcion;
    }

}
