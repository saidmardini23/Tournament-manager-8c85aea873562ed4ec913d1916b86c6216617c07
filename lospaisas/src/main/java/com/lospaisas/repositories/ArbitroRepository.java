package com.lospaisas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.Arbitro;

public interface ArbitroRepository extends JpaRepository<Arbitro, UUID> {

}