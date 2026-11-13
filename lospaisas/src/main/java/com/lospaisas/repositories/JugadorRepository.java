package com.lospaisas.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.Jugador;

public interface JugadorRepository extends JpaRepository<Jugador, UUID> {

    List<Jugador> findByEquipoId(UUID equipoId);
}