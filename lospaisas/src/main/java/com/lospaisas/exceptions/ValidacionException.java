package com.lospaisas.exceptions;

public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}