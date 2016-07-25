package com.treexor.auth.exceptions;

import org.springframework.http.HttpStatus;

public class LoginException extends Exception {
    private static final long serialVersionUID = 1L;

    private HttpStatus status;

    public LoginException(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getHttpStatus() {
        return status;
    }
}