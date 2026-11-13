package com.lospaisas.exceptions;

import java.net.URI;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice(basePackages = "com.lospaisas.controllers.web")
public class VistaExceptionHandler {

    private static final String MENSAJE_INTEGRIDAD = "No se puede completar la operación porque el registro tiene datos relacionados";
    private static final String MENSAJE_GENERICO = "Ha ocurrido un error inesperado. Intenta de nuevo";

    @ExceptionHandler({ RecursoNoEncontradoException.class, ValidacionException.class })
    public String handleNegocio(
            Exception ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        return volver(ex.getMessage(), request, redirectAttributes);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleIntegridad(
            DataIntegrityViolationException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        log.warn("Violación de integridad de datos", ex);
        return volver(MENSAJE_INTEGRIDAD, request, redirectAttributes);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(
            Exception ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        log.error("Error no controlado en la vista", ex);
        return volver(MENSAJE_GENERICO, request, redirectAttributes);
    }

    private String volver(String mensaje, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", mensaje);
        return "redirect:" + destino(request);
    }

    private String destino(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/";
        }

        try {
            String path = URI.create(referer).getRawPath();
            if (path == null || path.isBlank()) {
                return "/";
            }

            path = path.replaceFirst("/confirmar-eliminar$", "/opciones");

            if (path.equals(request.getRequestURI())) {
                return "/";
            }
            return path;
        } catch (IllegalArgumentException e) {
            return "/";
        }
    }
}