package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.request.ArbitroRequest;
import com.lospaisas.dto.response.ArbitroResponse;
import com.lospaisas.services.ArbitroService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/arbitros")
@CrossOrigin("*")
public class ArbitroController {

    private final ArbitroService arbitroService;

    public ArbitroController(ArbitroService arbitroService) {
        this.arbitroService = arbitroService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArbitroResponse crearArbitro(@Valid @RequestBody ArbitroRequest request) {
        return arbitroService.crearArbitro(request);
    }

    @GetMapping
    public List<ArbitroResponse> listarArbitros() {
        return arbitroService.listarArbitros();
    }

    @GetMapping("/{id}")
    public ArbitroResponse buscarPorId(@PathVariable UUID id) {
        return arbitroService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ArbitroResponse actualizarArbitro(
            @PathVariable UUID id,
            @Valid @RequestBody ArbitroRequest request) {

        return arbitroService.actualizarArbitro(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarArbitro(@PathVariable UUID id) {
        arbitroService.eliminarArbitro(id);
    }
}