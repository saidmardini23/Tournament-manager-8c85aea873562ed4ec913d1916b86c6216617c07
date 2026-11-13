package com.lospaisas.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.Rol;

public interface RolRepository extends JpaRepository<Rol, UUID> {

}