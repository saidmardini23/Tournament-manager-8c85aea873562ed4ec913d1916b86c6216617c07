package com.lospaisas.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

        @ExceptionHandler(RecursoNoEncontradoException.class)
        public ResponseEntity<Map<String, String>> handleNoEncontrado(RecursoNoEncontradoException ex) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", ex.getMessage()));
        }

        @ExceptionHandler(ValidacionException.class)
        public ResponseEntity<Map<String, String>> handleValidacion(ValidacionException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", ex.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidacionDTO(MethodArgumentNotValidException ex) {
                Map<String, String> errores = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, String>> handleBodyIlegible(HttpMessageNotReadableException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error",
                                                "El cuerpo de la petición es inválido o tiene un formato incorrecto (revisa JSON, enums, fechas y UUIDs)"));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<Map<String, String>> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "El parámetro '" + ex.getName() + "' tiene un formato inválido"));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, String>> handleIntegridad(DataIntegrityViolationException ex) {
                log.warn("Violación de integridad de datos", ex);
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(Map.of("error",
                                                "La operación viola una restricción de datos (registro duplicado o con relaciones activas)"));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
                if (ex instanceof ErrorResponse errorResponse) {
                        String detalle = errorResponse.getBody().getDetail();
                        return ResponseEntity
                                        .status(errorResponse.getStatusCode())
                                        .body(Map.of("error", detalle != null ? detalle : "Solicitud inválida"));
                }

                log.error("Error interno no controlado", ex);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Ha ocurrido un error interno en el servidor"));
        }
}
