package com.lospaisas.models.entity;

import java.util.UUID;

import com.lospaisas.models.enums.Posiciones;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "jugadores")
@Getter
@Setter
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_equipo", nullable = false)
    private Equipo equipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Posiciones posicion;

    public Jugador() {
    }

    public Jugador(UUID id, String nombre, String apellido, Usuario usuario, Equipo equipo, Posiciones posicion) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        this.equipo = equipo;
        this.posicion = posicion;
    }
}
