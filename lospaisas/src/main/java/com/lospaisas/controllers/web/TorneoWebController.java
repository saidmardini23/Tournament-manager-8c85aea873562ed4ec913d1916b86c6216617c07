package com.lospaisas.controllers.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lospaisas.dto.request.TorneoRequest;
import com.lospaisas.dto.response.EquipoResponse;
import com.lospaisas.dto.response.TorneoResponse;
import com.lospaisas.services.EquipoService;
import com.lospaisas.services.EstadisticaJugadorService;
import com.lospaisas.services.TablaPosicionService;
import com.lospaisas.services.TorneoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping({ "/torneos", "/admin/torneos" })
public class TorneoWebController {

    private static final String VISTA_FORMULARIO = "torneo/formularioTorneo";

    private final TorneoService torneoService;
    private final EquipoService equipoService;
    private final TablaPosicionService tablaPosicionService;
    private final EstadisticaJugadorService estadisticaJugadorService;

    public TorneoWebController(
            TorneoService torneoService,
            EquipoService equipoService,
            TablaPosicionService tablaPosicionService,
            EstadisticaJugadorService estadisticaJugadorService) {
        this.torneoService = torneoService;
        this.equipoService = equipoService;
        this.tablaPosicionService = tablaPosicionService;
        this.estadisticaJugadorService = estadisticaJugadorService;
    }

    @GetMapping
    public String listarTorneos(@RequestParam(name = "buscar", required = false) String buscar, Model model) {
        model.addAttribute("torneos", torneoService.listarTorneos(buscar));
        model.addAttribute("buscar", buscar);
        return "torneo/listaTorneo";
    }

    @GetMapping("/{id}")
    public String verMenuTorneo(@PathVariable UUID id, Model model) {
        TorneoResponse torneo = torneoService.buscarPorId(id);
        List<EquipoResponse> equipos = equipoService.listarEquipos(id);
        
        model.addAttribute("torneo", torneo);
        model.addAttribute("equipos", equipos);
        model.addAttribute("tablaPosiciones", tablaPosicionService.obtenerTablaCompleta(id, null));
        return "torneo/menuTorneo";
    }

    @GetMapping("/{id}/fixture")
    public String verFixture(@PathVariable UUID id) {
        return "redirect:/jornadas?torneoId=" + id;
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        TorneoRequest torneo = new TorneoRequest(
                "",
                "",
                LocalDate.now(),
                null,
                "PLANIFICADO",
                "LIGA");
        model.addAttribute("torneo", torneo);
        model.addAttribute("esEdicion", false);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nuevo")
    public String crearTorneo(
            @Valid @ModelAttribute("torneo") TorneoRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("esEdicion", false);
            return VISTA_FORMULARIO;
        }
        TorneoResponse nuevoTorneo = torneoService.crearTorneo(request);
        return "redirect:/torneos/" + nuevoTorneo.id();
    }

    @GetMapping("/{id}/opciones")
    public String verOpcionesTorneo(@PathVariable UUID id, Model model) {
        model.addAttribute("torneo", torneoService.buscarPorId(id));
        return "torneo/opcionesTorneo";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable UUID id, Model model) {
        TorneoResponse torneo = torneoService.buscarPorId(id);
        TorneoRequest request = new TorneoRequest(
                torneo.nombre(),
                torneo.descripcion(),
                torneo.fechaInicio(),
                torneo.fechaFin(),
                torneo.estado(),
                torneo.formato());
        model.addAttribute("torneo", request);
        model.addAttribute("torneoId", id);
        model.addAttribute("esEdicion", true);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}/editar")
    public String actualizarTorneo(
            @PathVariable UUID id,
            @Valid @ModelAttribute("torneo") TorneoRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("torneoId", id);
            model.addAttribute("esEdicion", true);
            return VISTA_FORMULARIO;
        }
        torneoService.actualizarTorneo(id, request);
        return "redirect:/torneos/" + id;
    }

    @GetMapping("/{id}/confirmar-eliminar")
    public String mostrarConfirmacionEliminar(@PathVariable UUID id, Model model) {
        model.addAttribute("torneo", torneoService.buscarPorId(id));
        return "torneo/confirmarEliminarTorneo";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarTorneo(@PathVariable UUID id) {
        torneoService.eliminarTorneo(id);
        return "redirect:/torneos";
    }

    @GetMapping("/{id}/rankings")
    public String verRankingsYPosiciones(
            @PathVariable UUID id,
            @RequestParam(name = "tab", required = false, defaultValue = "posiciones") String tab,
            @RequestParam(name = "equipoId", required = false) UUID equipoId,
            Model model) {
        TorneoResponse torneo = torneoService.buscarPorId(id);
        List<EquipoResponse> equipos = equipoService.listarEquipos(id);
        model.addAttribute("torneo", torneo);
        model.addAttribute("equipos", equipos);
        model.addAttribute("equipoIdSeleccionado", equipoId);
        model.addAttribute("tabActiva", tab);
        model.addAttribute("tablaPosiciones", tablaPosicionService.obtenerTablaCompleta(id, equipoId));
        model.addAttribute("goleadores", estadisticaJugadorService.obtenerRankingGoleadores(id, equipoId));
        model.addAttribute("asistentes", estadisticaJugadorService.obtenerRankingAsistencias(id, equipoId));
        return "torneo/rankings";
    }
}