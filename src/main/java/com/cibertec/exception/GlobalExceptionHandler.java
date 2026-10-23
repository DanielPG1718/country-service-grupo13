package com.cibertec.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaisNoEncontradoException.class)
    public ResponseEntity<?> manejarPaisNoEncontrado(PaisNoEncontradoException ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        error.put("estado", HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CountryApiException.class)
    public ResponseEntity<?> manejarCountryApi(CountryApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of(
                "mensaje", ex.getMessage(),
                "estado", ex.getStatus().value()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> manejarErrorGeneral(Exception ex) {

        Map<String, Object> error = new HashMap<>();
        error.put("mensaje", "Error interno del servidor: " + ex.getMessage());
        error.put("estado", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}