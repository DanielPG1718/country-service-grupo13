package com.cibertec.exception;

import org.springframework.http.HttpStatus;

public class CountryApiException extends RuntimeException {
    private final HttpStatus status;

    public CountryApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
