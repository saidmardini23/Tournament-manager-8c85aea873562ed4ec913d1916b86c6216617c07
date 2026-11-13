package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.request.CambiarEstadoRequest;
import com.lospaisas.dto.request.MarcadorRequest;
import com.lospaisas.dto.request.PartidoRequest;
import com.lospaisas.dto.response.PartidoResponse;
import com.lospaisas.services.PartidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/partidos")
@CrossOrigin("*")
public class PartidoController {

    private final PartidoService partidoService;

    public PartidoController(PartidoService partidoService) {
        this.partidoService = partidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartidoResponse crearPartido(
            @Valid @RequestBody PartidoRequest request) {

        return partidoService.crearPartido(request);
    }

    @GetMapping
    public List<PartidoResponse> listarPartidos() {

        return partidoService.listarPartidos();
    }

    @GetMapping("/{id}")
    public PartidoResponse buscarPorId(
            @PathVariable UUID id) {

        return partidoService.buscarPorId(id);
    }

    @PutMapping("/{id}/estado")
    public PartidoResponse cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarEstadoRequest request) {

        return partidoService.cambiarEstado(id, request.estado());
    }

    @PutMapping("/{id}/marcador")
    public PartidoResponse actualizarMarcador(
            @PathVariable UUID id,
            @Valid @RequestBody MarcadorRequest request) {

        return partidoService.actualizarMarcador(
                id,
                request.golesLocal(),
                request.golesVisitante());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPartido(
            @PathVariable UUID id) {

        partidoService.eliminarPartido(id);
    }
}
