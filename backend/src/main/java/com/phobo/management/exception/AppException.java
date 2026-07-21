package com.phobo.management.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public AppException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public AppException(String message, String errorCode, HttpStatus status) {
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
