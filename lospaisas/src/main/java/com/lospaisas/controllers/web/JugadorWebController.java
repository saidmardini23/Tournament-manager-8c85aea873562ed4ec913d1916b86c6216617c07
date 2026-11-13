package com.lospaisas.controllers.web;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lospaisas.dto.request.JugadorRequest;
import com.lospaisas.dto.response.JugadorResponse;
import com.lospaisas.models.enums.Posiciones;
import com.lospaisas.services.EquipoService;
import com.lospaisas.services.EstadisticaJugadorService;
import com.lospaisas.services.JugadorService;

@Controller
@RequestMapping({ "/jugadores", "/admin/jugadores" })
public class JugadorWebController {

    private final JugadorService jugadorService;
    private final EquipoService equipoService;
    private final EstadisticaJugadorService estadisticaJugadorService;

    public JugadorWebController(
            JugadorService jugadorService,
            EquipoService equipoService,
            EstadisticaJugadorService estadisticaJugadorService) {
        this.jugadorService = jugadorService;
        this.equipoService = equipoService;
        this.estadisticaJugadorService = estadisticaJugadorService;
    }

    @GetMapping("/{id}")
    public String verMenuJugador(@PathVariable UUID id, Model model) {
        model.addAttribute("jugador", jugadorService.buscarPorId(id));
        model.addAttribute("estadisticas", estadisticaJugadorService.obtenerEstadisticaPorJugador(id));
        return "jugador/menuJugador";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(
            @RequestParam(name = "equipoId", required = false) UUID equipoId,
            Model model) {

        model.addAttribute("jugador", new JugadorRequest("", "", equipoId, null));
        model.addAttribute("equipos", equipoService.listarEquipos());
        model.addAttribute("posiciones", Posiciones.values());
        model.addAttribute("esEdicion", false);

        return "jugador/formularioJugador";
    }

    @PostMapping("/nuevo")
    public String crearJugador(@ModelAttribute JugadorRequest request) {
        JugadorResponse nuevoJugador = jugadorService.crearJugador(request);
        return "redirect:/jugadores/" + nuevoJugador.id();
    }

    @GetMapping("/{id}/opciones")
    public String verOpcionesJugador(@PathVariable UUID id, Model model) {
        model.addAttribute("jugador", jugadorService.buscarPorId(id));
        return "jugador/opcionesJugador";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable UUID id, Model model) {
        JugadorResponse jugador = jugadorService.buscarPorId(id);
        JugadorRequest request = new JugadorRequest(
                jugador.nombre(),
                jugador.apellido(),
                jugador.equipoId(),
                jugador.posicion());

        model.addAttribute("jugador", request);
        model.addAttribute("jugadorId", id);
        model.addAttribute("equipos", equipoService.listarEquipos());
        model.addAttribute("posiciones", Posiciones.values());
        model.addAttribute("esEdicion", true);

        return "jugador/formularioJugador";
    }

    @PostMapping("/{id}/editar")
    public String actualizarJugador(
            @PathVariable UUID id,
            @ModelAttribute JugadorRequest request) {

        jugadorService.actualizarJugador(id, request);
        return "redirect:/jugadores/" + id;
    }

    @GetMapping("/{id}/confirmar-eliminar")
    public String mostrarConfirmacionEliminar(@PathVariable UUID id, Model model) {
        model.addAttribute("jugador", jugadorService.buscarPorId(id));
        return "jugador/confirmarEliminarJugador";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarJugador(@PathVariable UUID id) {
        JugadorResponse jugador = jugadorService.buscarPorId(id);
        jugadorService.eliminarJugador(id);
        return jugador.equipoId() != null ? "redirect:/equipos/" + jugador.equipoId() : "redirect:/torneos";
    }
}