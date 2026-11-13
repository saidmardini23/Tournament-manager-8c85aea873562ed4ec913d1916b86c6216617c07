package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.request.RolRequest;
import com.lospaisas.dto.response.RolResponse;
import com.lospaisas.services.RolService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin("*")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolResponse crearRol(@Valid @RequestBody RolRequest request) {
        return rolService.crearRol(request);
    }

    @GetMapping
    public List<RolResponse> listarRoles() {
        return rolService.listarRoles();
    }

    @GetMapping("/{id}")
    public RolResponse buscarPorId(@PathVariable UUID id) {
        return rolService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public RolResponse actualizarRol(
            @PathVariable UUID id,
            @Valid @RequestBody RolRequest request) {

        return rolService.actualizarRol(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarRol(@PathVariable UUID id) {
        rolService.eliminarRol(id);
    }
}
