package com.lospaisas.models.entity;

import java.util.UUID;

import com.lospaisas.models.enums.Roles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Roles nombre;

    public Rol() {
    }

    public Rol(UUID id, Roles nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
