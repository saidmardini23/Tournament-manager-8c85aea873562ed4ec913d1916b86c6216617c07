package com.lospaisas.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.Equipo;

public interface EquipoRepository extends JpaRepository<Equipo, UUID> {

    List<Equipo> findByTorneoId(UUID torneoId);

}