package com.lospaisas.controllers.web;

import java.time.LocalDateTime;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lospaisas.dto.request.PartidoRequest;
import com.lospaisas.dto.response.EventoPartidoResponse;
import com.lospaisas.dto.response.JornadaResponse;
import com.lospaisas.dto.response.PartidoResponse;
import com.lospaisas.models.enums.EstadoPartido;
import com.lospaisas.services.ArbitroService;
import com.lospaisas.services.EquipoService;
import com.lospaisas.services.EventoPartidoService;
import com.lospaisas.services.JornadaService;
import com.lospaisas.services.PartidoService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping({ "/partidos", "/admin/partidos" })
public class PartidoWebController {

    private static final String VISTA_FORMULARIO = "partido/formularioPartido";

    private final PartidoService partidoService;
    private final JornadaService jornadaService;
    private final EquipoService equipoService;
    private final ArbitroService arbitroService;
    private final EventoPartidoService eventoPartidoService;

    public PartidoWebController(PartidoService partidoService,
                                JornadaService jornadaService,
                                EquipoService equipoService,
                                ArbitroService arbitroService,
                                EventoPartidoService eventoPartidoService) {
        this.partidoService = partidoService;
        this.jornadaService = jornadaService;
        this.equipoService = equipoService;
        this.arbitroService = arbitroService;
        this.eventoPartidoService = eventoPartidoService;
    }

    // 1. Listar partidos
    @GetMapping
    public String listarPartidos(@RequestParam(name = "jornadaId", required = false) UUID jornadaId, Model model) {
        if (jornadaId != null) {
            JornadaResponse jornada = jornadaService.buscarPorId(jornadaId);
            model.addAttribute("jornada", jornada);
            model.addAttribute("jornadaId", jornadaId);
            model.addAttribute("partidos", partidoService.listarPorJornada(jornadaId));
        } else {
            model.addAttribute("jornada", null);
            model.addAttribute("jornadaId", null);
            model.addAttribute("partidos", partidoService.listarPartidos());
        }
        return "partido/listaPartidos";
    }

    // 2. Formulario Crear Partido (Ubicado ANTES de /{id})
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(@RequestParam(name = "jornadaId", required = false) UUID jornadaId, Model model) {
        PartidoRequest request = new PartidoRequest(jornadaId, null, null, null, LocalDateTime.now());
        model.addAttribute("partido", request);
        cargarCatalogosFormulario(model, null, jornadaId);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nuevo")
    public String crearPartido(@Valid @ModelAttribute("partido") PartidoRequest request,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            cargarCatalogosFormulario(model, null, request.jornadaId());
            return VISTA_FORMULARIO;
        }

        try {
            PartidoResponse nuevoPartido = partidoService.crearPartido(request);
            return "redirect:/partidos/" + nuevoPartido.id();
        } catch (Exception e) {
            log.error("Error al programar partido", e);
            model.addAttribute("error", e.getMessage());
            cargarCatalogosFormulario(model, null, request.jornadaId());
            return VISTA_FORMULARIO;
        }
    }

    // 3. Ver detalle del partido
    @GetMapping("/{id}")
    public String verMenuPartido(@PathVariable UUID id, Model model) {
        PartidoResponse partido = partidoService.buscarPorId(id);
        List<EventoPartidoResponse> eventos = eventoPartidoService.listarPorPartido(id);

        model.addAttribute("partido", partido);
        model.addAttribute("eventos", eventos);
        model.addAttribute("estados", EstadoPartido.values());
        return "partido/menuPartido";
    }

    // 4. Opciones del partido
    @GetMapping("/{id}/opciones")
    public String verOpcionesPartido(@PathVariable UUID id, Model model) {
        model.addAttribute("partido", partidoService.buscarPorId(id));
        return "partido/opcionesPartido";
    }

    // 5. Cambiar Estado
    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable UUID id,
                                @RequestParam("estado") EstadoPartido estado,
                                RedirectAttributes redirectAttributes) {
        try {
            partidoService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("exito", "Estado actualizado a " + estado);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/partidos/" + id;
    }

    // 6. Actualizar Marcador
    @PostMapping("/{id}/marcador")
    public String actualizarMarcador(@PathVariable UUID id,
                                     @RequestParam("golesLocal") int golesLocal,
                                     @RequestParam("golesVisitante") int golesVisitante,
                                     RedirectAttributes redirectAttributes) {
        try {
            partidoService.actualizarMarcador(id, golesLocal, golesVisitante);
            redirectAttributes.addFlashAttribute("exito", "Marcador actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/partidos/" + id;
    }

    // 7. Confirmación y Eliminación
    @GetMapping("/{id}/confirmar-eliminar")
    public String mostrarConfirmacionEliminar(@PathVariable UUID id, Model model) {
        model.addAttribute("partido", partidoService.buscarPorId(id));
        return "partido/confirmarEliminarPartido";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarPartido(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        PartidoResponse partido = partidoService.buscarPorId(id);
        UUID jornadaId = partido.jornadaId();

        try {
            partidoService.eliminarPartido(id);
            return jornadaId != null ? "redirect:/jornadas/" + jornadaId : "redirect:/partidos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el partido: " + e.getMessage());
            return "redirect:/admin/partidos/" + id + "/opciones";
        }
    }

    private void cargarCatalogosFormulario(Model model, UUID partidoId, UUID jornadaId) {
        model.addAttribute("partidoId", partidoId);
        model.addAttribute("esEdicion", partidoId != null);
        model.addAttribute("jornadaId", jornadaId);

        if (jornadaId != null) {
            JornadaResponse jornada = jornadaService.buscarPorId(jornadaId);
            model.addAttribute("equipos", equipoService.listarEquipos(jornada.torneoId()));
            model.addAttribute("jornadas", List.of(jornada));
        } else {
            model.addAttribute("jornadas", jornadaService.listarJornadas());
            model.addAttribute("equipos", equipoService.listarEquipos());
        }

        model.addAttribute("arbitros", arbitroService.listarArbitros());
    }
}