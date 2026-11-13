package com.lospaisas.controllers.web;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lospaisas.dto.request.EquipoRequest;
import com.lospaisas.dto.response.EquipoResponse;
import com.lospaisas.dto.response.EstadisticaJugadorResponse;
import com.lospaisas.dto.response.JugadorResponse;
import com.lospaisas.dto.response.TorneoResponse;
import com.lospaisas.services.EquipoService;
import com.lospaisas.services.EstadisticaJugadorService;
import com.lospaisas.services.JugadorService;
import com.lospaisas.services.TorneoService;

@Controller
@RequestMapping({ "/equipos", "/admin/equipos" })
public class EquipoWebController {

    private final EquipoService equipoService;
    private final TorneoService torneoService;
    private final JugadorService jugadorService;
    private final EstadisticaJugadorService estadisticaJugadorService;

    public EquipoWebController(
            EquipoService equipoService,
            TorneoService torneoService,
            JugadorService jugadorService,
            EstadisticaJugadorService estadisticaJugadorService) {
        this.equipoService = equipoService;
        this.torneoService = torneoService;
        this.jugadorService = jugadorService;
        this.estadisticaJugadorService = estadisticaJugadorService;
    }

    @GetMapping
    public String listarEquipos() {
        return "redirect:/torneos";
    }

    @GetMapping("/{id}")
    public String verMenuEquipo(
            @PathVariable UUID id,
            @RequestParam(name = "buscar", required = false) String buscar,
            Model model) {

        EquipoResponse equipo = equipoService.buscarPorId(id);
        List<JugadorResponse> jugadores = jugadorService.buscarPorEquipo(id, buscar);

        List<EstadisticaJugadorResponse> goleadores = estadisticaJugadorService
                .obtenerRankingGoleadores(equipo.torneoId(), id);
        List<EstadisticaJugadorResponse> asistentes = estadisticaJugadorService
                .obtenerRankingAsistencias(equipo.torneoId(), id);

        EstadisticaJugadorResponse maxGoleador = goleadores.stream().findFirst().orElse(null);
        EstadisticaJugadorResponse maxAsistente = asistentes.stream().findFirst().orElse(null);

        model.addAttribute("equipo", equipo);
        model.addAttribute("jugadores", jugadores);
        model.addAttribute("buscar", buscar);
        model.addAttribute("goleadores", goleadores);
        model.addAttribute("asistentes", asistentes);
        model.addAttribute("maxGoleador", maxGoleador);
        model.addAttribute("maxAsistente", maxAsistente);

        return "equipo/menuEquipo";
    }

    @GetMapping({ "/{id}/goleadores", "/{id}/goles" })
    public String verGoleadoresEquipo(@PathVariable UUID id, Model model) {
        EquipoResponse equipo = equipoService.buscarPorId(id);
        List<EstadisticaJugadorResponse> goleadores = estadisticaJugadorService
                .obtenerRankingGoleadores(equipo.torneoId(), id);

        model.addAttribute("equipo", equipo);
        model.addAttribute("goleadores", goleadores);
        return "equipo/goleadoresEquipo";
    }

    @GetMapping({ "/{id}/asistentes", "/{id}/asistencias" })
    public String verAsistentesEquipo(@PathVariable UUID id, Model model) {
        EquipoResponse equipo = equipoService.buscarPorId(id);
        List<EstadisticaJugadorResponse> asistentes = estadisticaJugadorService
                .obtenerRankingAsistencias(equipo.torneoId(), id);

        model.addAttribute("equipo", equipo);
        model.addAttribute("asistentes", asistentes);
        return "equipo/asistentesEquipo";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(
            @RequestParam(name = "torneoId", required = false) UUID torneoId,
            Model model) {

        List<TorneoResponse> torneos = torneoService.listarTorneos();

        model.addAttribute("equipo", new EquipoRequest("", "", torneoId));
        model.addAttribute("equipoId", null);
        model.addAttribute("torneoId", torneoId);
        model.addAttribute("torneos", torneos);
        model.addAttribute("esEdicion", false);

        return "equipo/formularioEquipo";
    }

    @PostMapping("/nuevo")
    public String crearEquipo(@ModelAttribute EquipoRequest request) {
        equipoService.crearEquipo(request);
        return "redirect:/torneos/" + request.torneoId();
    }

    @GetMapping("/{id}/opciones")
    public String verOpcionesEquipo(@PathVariable UUID id, Model model) {
        model.addAttribute("equipo", equipoService.buscarPorId(id));
        return "equipo/opcionesEquipo";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable UUID id, Model model) {
        EquipoResponse equipo = equipoService.buscarPorId(id);
        EquipoRequest request = new EquipoRequest(
                equipo.nombre(),
                equipo.entrenador(),
                equipo.torneoId());

        model.addAttribute("equipo", request);
        model.addAttribute("equipoId", id);
        model.addAttribute("torneoId", equipo.torneoId());
        model.addAttribute("torneos", torneoService.listarTorneos());
        model.addAttribute("esEdicion", true);

        return "equipo/formularioEquipo";
    }

    @PostMapping("/{id}/editar")
    public String actualizarEquipo(
            @PathVariable UUID id,
            @ModelAttribute EquipoRequest request) {

        equipoService.actualizarEquipo(id, request);
        return "redirect:/equipos/" + id;
    }

    @GetMapping("/{id}/confirmar-eliminar")
    public String mostrarConfirmacionEliminar(@PathVariable UUID id, Model model) {
        model.addAttribute("equipo", equipoService.buscarPorId(id));
        return "equipo/confirmarEliminarEquipo";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarEquipo(@PathVariable UUID id) {
        EquipoResponse equipo = equipoService.buscarPorId(id);
        equipoService.eliminarEquipo(id);
        return equipo.torneoId() != null ? "redirect:/torneos/" + equipo.torneoId() : "redirect:/torneos";
    }
}