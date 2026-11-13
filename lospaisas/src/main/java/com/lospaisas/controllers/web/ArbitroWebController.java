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

import com.lospaisas.dto.request.ArbitroRequest;
import com.lospaisas.dto.response.ArbitroResponse;
import com.lospaisas.services.ArbitroService;

@Controller
@RequestMapping({ "/arbitros", "/admin/arbitros" })
public class ArbitroWebController {

    private final ArbitroService arbitroService;

    public ArbitroWebController(ArbitroService arbitroService) {
        this.arbitroService = arbitroService;
    }

    @GetMapping
    public String listarArbitros(
            @RequestParam(name = "buscar", required = false) String buscar,
            Model model) {

        model.addAttribute("arbitros", arbitroService.listarArbitros(buscar));
        model.addAttribute("buscar", buscar);
        return "arbitro/listaArbitros";
    }

    @GetMapping("/{id}")
    public String verMenuArbitro(@PathVariable UUID id, Model model) {
        model.addAttribute("arbitro", arbitroService.buscarPorId(id));
        return "arbitro/menuArbitro";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("arbitro", new ArbitroRequest("", "", 0, ""));
        model.addAttribute("esEdicion", false);
        return "arbitro/formularioArbitro";
    }

    @PostMapping("/nuevo")
    public String crearArbitro(@ModelAttribute ArbitroRequest request) {
        ArbitroResponse nuevoArbitro = arbitroService.crearArbitro(request);
        return "redirect:/arbitros/" + nuevoArbitro.id();
    }

    @GetMapping("/{id}/opciones")
    public String verOpcionesArbitro(@PathVariable UUID id, Model model) {
        model.addAttribute("arbitro", arbitroService.buscarPorId(id));
        return "arbitro/opcionesArbitro";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable UUID id, Model model) {
        ArbitroResponse arbitro = arbitroService.buscarPorId(id);
        ArbitroRequest request = new ArbitroRequest(
                arbitro.nombre(),
                arbitro.apellido(),
                arbitro.experiencia(),
                arbitro.licencia());

        model.addAttribute("arbitro", request);
        model.addAttribute("arbitroId", id);
        model.addAttribute("esEdicion", true);
        return "arbitro/formularioArbitro";
    }

    @PostMapping("/{id}/editar")
    public String actualizarArbitro(
            @PathVariable UUID id,
            @ModelAttribute ArbitroRequest request) {

        arbitroService.actualizarArbitro(id, request);
        return "redirect:/arbitros/" + id;
    }

    @GetMapping("/{id}/confirmar-eliminar")
    public String mostrarConfirmacionEliminar(@PathVariable UUID id, Model model) {
        model.addAttribute("arbitro", arbitroService.buscarPorId(id));
        return "arbitro/confirmarEliminarArbitro";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarArbitro(@PathVariable UUID id) {
        arbitroService.eliminarArbitro(id);
        return "redirect:/arbitros";
    }
}