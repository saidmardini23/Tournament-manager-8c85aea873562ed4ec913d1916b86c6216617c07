package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.request.JugadorRequest;
import com.lospaisas.dto.response.JugadorResponse;
import com.lospaisas.services.JugadorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jugadores")
@CrossOrigin("*")
public class JugadorController {

    private final JugadorService jugadorService;

    public JugadorController(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JugadorResponse crearJugador(@Valid @RequestBody JugadorRequest request) {

        return jugadorService.crearJugador(request);
    }

    @GetMapping
    public List<JugadorResponse> listarJugadores() {

        return jugadorService.listarJugadores();
    }

    @GetMapping("/{id}")
    public JugadorResponse buscarPorId(@PathVariable UUID id) {

        return jugadorService.buscarPorId(id);
    }

    @GetMapping("/equipo/{equipoId}")
    public List<JugadorResponse> buscarPorEquipo(
            @PathVariable UUID equipoId) {

        return jugadorService.buscarPorEquipo(equipoId);
    }

    @PutMapping("/{id}")
    public JugadorResponse actualizarJugador(
            @PathVariable UUID id,
            @Valid @RequestBody JugadorRequest request) {

        return jugadorService.actualizarJugador(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarJugador(@PathVariable UUID id) {

        jugadorService.eliminarJugador(id);
    }
}
