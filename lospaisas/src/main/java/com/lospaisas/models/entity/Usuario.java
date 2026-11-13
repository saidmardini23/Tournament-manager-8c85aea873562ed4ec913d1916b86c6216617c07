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
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;

    public Usuario() {
    }

    public Usuario(UUID id, String nombre, String email, String passwordHash, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
}
