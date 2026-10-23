package com.cibertec.exception;

public class PaisNoEncontradoException extends RuntimeException {

    public PaisNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}