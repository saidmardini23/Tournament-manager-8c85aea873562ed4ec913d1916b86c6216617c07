package com.lospaisas.controllers.web;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lospaisas.dto.response.PartidoResponse;
import com.lospaisas.services.EquipoService;
import com.lospaisas.services.JugadorService;
import com.lospaisas.services.PartidoService;
import com.lospaisas.services.TorneoService;

@Controller
@RequestMapping("/admin")
public class DashboardWebController {

    private final TorneoService torneoService;
    private final EquipoService equipoService;
    private final JugadorService jugadorService;
    private final PartidoService partidoService;

    public DashboardWebController(
            TorneoService torneoService,
            EquipoService equipoService,
            JugadorService jugadorService,
            PartidoService partidoService) {
        this.torneoService = torneoService;
        this.equipoService = equipoService;
        this.jugadorService = jugadorService;
        this.partidoService = partidoService;
    }

    @GetMapping({ "", "/", "/dashboard" })
    public String verDashboard(Model model) {
        List<PartidoResponse> todosPartidos = partidoService.listarPartidos();

        model.addAttribute("totalTorneos", torneoService.listarTorneos().size());
        model.addAttribute("totalEquipos", equipoService.listarEquipos().size());
        model.addAttribute("totalJugadores", jugadorService.listarJugadores().size());
        model.addAttribute("totalPartidos", todosPartidos.size());
        model.addAttribute("proximosPartidos", partidoService.obtenerProximosPartidos(5));

        return "admin/dashboard";
    }
}