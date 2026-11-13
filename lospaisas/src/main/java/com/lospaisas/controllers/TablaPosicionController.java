package com.lospaisas.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.lospaisas.dto.response.TablaPosicionResponse;
import com.lospaisas.services.TablaPosicionService;

@RestController
@RequestMapping("/api/tabla-posiciones")
@CrossOrigin("*")
public class TablaPosicionController {

    private final TablaPosicionService tablaPosicionService;

    public TablaPosicionController(TablaPosicionService tablaPosicionService) {
        this.tablaPosicionService = tablaPosicionService;
    }

    @GetMapping("/{torneoId}")
    public List<TablaPosicionResponse> obtenerTabla(
            @PathVariable UUID torneoId) {

        return tablaPosicionService.obtenerTabla(torneoId);
    }
}
