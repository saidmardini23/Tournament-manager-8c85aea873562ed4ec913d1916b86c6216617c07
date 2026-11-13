package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.request.UsuarioRequest;
import com.lospaisas.dto.request.UsuarioUpdateRequest;
import com.lospaisas.dto.response.UsuarioResponse;
import com.lospaisas.services.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin("*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crearUsuario(request);
    }

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizarUsuario(
            @PathVariable UUID id,
            @Valid @RequestBody UsuarioUpdateRequest request) {

        return usuarioService.actualizarUsuario(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable UUID id) {
        usuarioService.eliminarUsuario(id);
    }
}
