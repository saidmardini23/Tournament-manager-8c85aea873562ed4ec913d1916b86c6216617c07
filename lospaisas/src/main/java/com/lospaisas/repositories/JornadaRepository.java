package com.lospaisas.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.Jornada;

public interface JornadaRepository extends JpaRepository<Jornada, UUID> {

    List<Jornada> findByTorneoId(UUID torneoId);

}