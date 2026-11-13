package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.request.EventoPartidoRequest;
import com.lospaisas.dto.request.EventoPartidoUpdateRequest;
import com.lospaisas.dto.response.EventoPartidoResponse;
import com.lospaisas.services.EventoPartidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/eventos-partido")
@CrossOrigin("*")
public class EventoPartidoController {

    private final EventoPartidoService eventoPartidoService;

    public EventoPartidoController(EventoPartidoService eventoPartidoService) {
        this.eventoPartidoService = eventoPartidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventoPartidoResponse registrarEvento(
            @Valid @RequestBody EventoPartidoRequest request) {

        return eventoPartidoService.registrarEvento(request);
    }

    @GetMapping("/partido/{partidoId}")
    public List<EventoPartidoResponse> listarPorPartido(
            @PathVariable UUID partidoId) {

        return eventoPartidoService.listarPorPartido(partidoId);
    }

    @PutMapping("/{id}")
    public EventoPartidoResponse actualizarEvento(
            @PathVariable UUID id,
            @Valid @RequestBody EventoPartidoUpdateRequest request) {

        return eventoPartidoService.actualizarEvento(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarEvento(
            @PathVariable UUID id) {

        eventoPartidoService.eliminarEvento(id);
    }
}
