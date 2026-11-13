package com.lospaisas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.Torneo;

public interface TorneoRepository extends JpaRepository<Torneo, UUID> {

}
