package com.lospaisas.controllers.web;

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

import com.lospaisas.dto.request.JornadaRequest;
import com.lospaisas.dto.response.JornadaResponse;
import com.lospaisas.dto.response.PartidoResponse;
import com.lospaisas.dto.response.TorneoResponse;
import com.lospaisas.services.JornadaService;
import com.lospaisas.services.PartidoService;
import com.lospaisas.services.TorneoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping({ "/jornadas", "/admin/jornadas" })
public class JornadaWebController {

    private static final String VISTA_FORMULARIO = "jornada/formularioJornada";

    private final JornadaService jornadaService;
    private final TorneoService torneoService;
    private final PartidoService partidoService;

    public JornadaWebController(JornadaService jornadaService,
                                TorneoService torneoService,
                                PartidoService partidoService) {
        this.jornadaService = jornadaService;
        this.torneoService = torneoService;
        this.partidoService = partidoService;
    }

    @GetMapping
    public String listarJornadas(@RequestParam(name = "torneoId", required = false) UUID torneoId, Model model) {
        if (torneoId != null) {
            TorneoResponse torneo = torneoService.buscarPorId(torneoId);
            model.addAttribute("torneo", torneo);
            model.addAttribute("torneoId", torneoId);
            model.addAttribute("jornadas", jornadaService.listarPorTorneo(torneoId));
        } else {
            model.addAttribute("torneo", null);
            model.addAttribute("torneoId", null);
            model.addAttribute("jornadas", jornadaService.listarJornadas());
        }
        return "jornada/listaJornadas";
    }

    @GetMapping("/{id}")
    public String verMenuJornada(@PathVariable UUID id, Model model) {
        JornadaResponse jornada = jornadaService.buscarPorId(id);
        TorneoResponse torneo = torneoService.buscarPorId(jornada.torneoId());

        // Partidos agrupados/filtrados por el ID de la jornada
        List<PartidoResponse> partidos = partidoService.listarPorJornada(id);

        model.addAttribute("jornada", jornada);
        model.addAttribute("torneo", torneo);
        model.addAttribute("partidos", partidos);
        return "jornada/menuJornada";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(@RequestParam(name = "torneoId", required = false) UUID torneoId, Model model) {
        JornadaRequest request = new JornadaRequest(torneoId, 1);
        model.addAttribute("jornada", request);
        cargarCatalogosFormulario(model, null, torneoId, false);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nuevo")
    public String crearJornada(@Valid @ModelAttribute("jornada") JornadaRequest request,
                               BindingResult bindingResult,
                               HttpServletRequest httpRequest,
                               Model model) {
        if (bindingResult.hasErrors()) {
            cargarCatalogosFormulario(model, null, request.torneoId(), false);
            return VISTA_FORMULARIO;
        }

        try {
            JornadaResponse nuevaJornada = jornadaService.crearJornada(request);
            return "redirect:/jornadas/" + nuevaJornada.id();
        } catch (Exception e) {
            log.error("Error al crear la jornada", e);
            model.addAttribute("error", e.getMessage());
            cargarCatalogosFormulario(model, null, request.torneoId(), false);
            return VISTA_FORMULARIO;
        }
    }

    @GetMapping("/{id}/opciones")
    public String verOpcionesJornada(@PathVariable UUID id, Model model) {
        model.addAttribute("jornada", jornadaService.buscarPorId(id));
        return "jornada/opcionesJornada";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable UUID id, Model model) {
        JornadaResponse jornada = jornadaService.buscarPorId(id);
        JornadaRequest request = new JornadaRequest(jornada.torneoId(), jornada.numeroJornada());

        model.addAttribute("jornada", request);
        cargarCatalogosFormulario(model, id, jornada.torneoId(), true);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}/editar")
    public String actualizarJornada(@PathVariable UUID id,
                                    @Valid @ModelAttribute("jornada") JornadaRequest request,
                                    BindingResult bindingResult,
                                    HttpServletRequest httpRequest,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            cargarCatalogosFormulario(model, id, request.torneoId(), true);
            return VISTA_FORMULARIO;
        }

        try {
            jornadaService.actualizarJornada(id, request);
            return "redirect:/jornadas/" + id;
        } catch (Exception e) {
            log.error("Error al actualizar la jornada", e);
            model.addAttribute("error", e.getMessage());
            cargarCatalogosFormulario(model, id, request.torneoId(), true);
            return VISTA_FORMULARIO;
        }
    }

    @GetMapping("/{id}/confirmar-eliminar")
    public String mostrarConfirmacionEliminar(@PathVariable UUID id, Model model) {
        model.addAttribute("jornada", jornadaService.buscarPorId(id));
        return "jornada/confirmarEliminarJornada";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarJornada(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        JornadaResponse jornada = jornadaService.buscarPorId(id);
        UUID torneoId = jornada.torneoId();

        try {
            jornadaService.eliminarJornada(id);
            return torneoId != null ? "redirect:/jornadas?torneoId=" + torneoId : "redirect:/torneos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la jornada: " + e.getMessage());
            return "redirect:/admin/jornadas/" + id + "/opciones";
        }
    }

    private void cargarCatalogosFormulario(Model model, UUID jornadaId, UUID torneoId, boolean esEdicion) {
        model.addAttribute("esEdicion", esEdicion);
        model.addAttribute("jornadaId", jornadaId);
        model.addAttribute("torneoId", torneoId);
        if (torneoId != null) {
            model.addAttribute("torneoActual", torneoService.buscarPorId(torneoId));
        } else {
            model.addAttribute("torneos", torneoService.listarTorneos());
        }
    }
}