package com.phobo.management.exception;

import org.springframework.http.HttpStatus;

public class AddressException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public AddressException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
