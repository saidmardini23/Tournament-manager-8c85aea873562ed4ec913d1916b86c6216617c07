package com.lospaisas.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lospaisas.models.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

}