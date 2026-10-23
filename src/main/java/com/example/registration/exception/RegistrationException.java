package com.example.registration.exception;

import org.springframework.http.HttpStatus;

/**
 * Expected business error translated to an HTTP status by the API layer.
 */
public class RegistrationException extends RuntimeException {
    private final HttpStatus status;

    public RegistrationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
