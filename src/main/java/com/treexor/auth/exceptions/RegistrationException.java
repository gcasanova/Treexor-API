package com.treexor.auth.exceptions;

import org.springframework.http.HttpStatus;

public class RegistrationException extends Exception {
    private static final long serialVersionUID = 1L;

    private HttpStatus status;

    public RegistrationException(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getHttpStatus() {
        return status;
    }
}