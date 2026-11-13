
package com.lospaisas.controllers;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.lospaisas.dto.response.PartidoResponse;
import com.lospaisas.models.enums.EstadoPartido;

@Controller
public class ComponentesWebController {

    @GetMapping("/_componentes")
    public String mostrarComponentes(Model model) {

        PartidoResponse partidoDemo = new PartidoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Atlético Norte",
                UUID.randomUUID(),
                "Deportivo Sur",
                null,
                "Árbitro de prueba",
                2,
                1,
                EstadoPartido.FINALIZADO,
                LocalDateTime.now()
        );

        model.addAttribute("partidoDemo", partidoDemo);

        return "public/_componentes";
    }
}
