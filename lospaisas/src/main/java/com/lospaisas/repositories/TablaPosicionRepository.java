package com.lospaisas.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.TablaPosicion;

public interface TablaPosicionRepository extends JpaRepository<TablaPosicion, UUID> {

    List<TablaPosicion> findByTorneoId(UUID torneoId);

}