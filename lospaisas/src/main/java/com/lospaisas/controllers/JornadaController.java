package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.request.JornadaRequest;
import com.lospaisas.dto.response.JornadaResponse;
import com.lospaisas.services.JornadaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jornadas")
@CrossOrigin("*")
public class JornadaController {

    private final JornadaService jornadaService;

    public JornadaController(JornadaService jornadaService) {
        this.jornadaService = jornadaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JornadaResponse crearJornada(
            @Valid @RequestBody JornadaRequest request) {

        return jornadaService.crearJornada(request);
    }

    @GetMapping
    public List<JornadaResponse> listarJornadas() {

        return jornadaService.listarJornadas();
    }

    @GetMapping("/torneo/{torneoId}")
    public List<JornadaResponse> listarPorTorneo(
            @PathVariable UUID torneoId) {

        return jornadaService.listarPorTorneo(torneoId);
    }

    @GetMapping("/{id}")
    public JornadaResponse buscarPorId(
            @PathVariable UUID id) {

        return jornadaService.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarJornada(
            @PathVariable UUID id) {

        jornadaService.eliminarJornada(id);
    }
}
