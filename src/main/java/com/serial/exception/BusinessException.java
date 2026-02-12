package com.serial.exception;

public class BusinessException extends RuntimeException {

    private final int httpStatus;

    public BusinessException(String message) {
        super(message);
        this.httpStatus = 400;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
