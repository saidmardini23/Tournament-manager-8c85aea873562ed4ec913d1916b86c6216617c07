package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.request.EquipoRequest;
import com.lospaisas.dto.response.EquipoResponse;
import com.lospaisas.services.EquipoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/equipos")
@CrossOrigin("*")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResponse crearEquipo(@Valid @RequestBody EquipoRequest request) {
        return equipoService.crearEquipo(request);
    }

    @GetMapping
    public List<EquipoResponse> listarEquipos() {
        return equipoService.listarEquipos();
    }

    @GetMapping("/{id}")
    public EquipoResponse buscarPorId(@PathVariable UUID id) {
        return equipoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public EquipoResponse actualizarEquipo(
            @PathVariable UUID id,
            @Valid @RequestBody EquipoRequest request) {

        return equipoService.actualizarEquipo(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarEquipo(@PathVariable UUID id) {
        equipoService.eliminarEquipo(id);
    }
}
