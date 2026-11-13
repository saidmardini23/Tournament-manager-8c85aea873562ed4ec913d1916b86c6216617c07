package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.response.EstadisticaJugadorResponse;
import com.lospaisas.services.EstadisticaJugadorService;

@RestController
@RequestMapping("/api/estadisticas-jugadores")
@CrossOrigin("*")
public class EstadisticaJugadorController {

    private final EstadisticaJugadorService estadisticaJugadorService;

    public EstadisticaJugadorController(EstadisticaJugadorService estadisticaJugadorService) {
        this.estadisticaJugadorService = estadisticaJugadorService;
    }

    @GetMapping("/goleadores/{torneoId}")
    public List<EstadisticaJugadorResponse> obtenerRankingGoleadores(
            @PathVariable UUID torneoId) {

        return estadisticaJugadorService
                .obtenerRankingGoleadores(torneoId);
    }

    @GetMapping("/asistencias/{torneoId}")
    public List<EstadisticaJugadorResponse> obtenerRankingAsistencias(
            @PathVariable UUID torneoId) {

        return estadisticaJugadorService
                .obtenerRankingAsistencias(torneoId);
    }
}
