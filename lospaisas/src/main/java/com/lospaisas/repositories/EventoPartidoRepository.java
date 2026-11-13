package com.lospaisas.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.EventoPartido;

public interface EventoPartidoRepository extends JpaRepository<EventoPartido, UUID> {

    List<EventoPartido> findByPartidoId(UUID partidoId);

}