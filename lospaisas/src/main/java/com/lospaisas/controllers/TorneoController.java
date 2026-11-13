package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.request.TorneoRequest;
import com.lospaisas.dto.response.TorneoResponse;
import com.lospaisas.services.TorneoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/torneos")
@CrossOrigin("*")
public class TorneoController {

    private final TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TorneoResponse crearTorneo(@Valid @RequestBody TorneoRequest request) {
        return torneoService.crearTorneo(request);
    }

    @GetMapping
    public List<TorneoResponse> listarTorneos() {
        return torneoService.listarTorneos();
    }

    @GetMapping("/{id}")
    public TorneoResponse buscarPorId(@PathVariable UUID id) {
        return torneoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public TorneoResponse actualizarTorneo(
            @PathVariable UUID id,
            @Valid @RequestBody TorneoRequest request) {

        return torneoService.actualizarTorneo(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarTorneo(@PathVariable UUID id) {
        torneoService.eliminarTorneo(id);
    }
}
