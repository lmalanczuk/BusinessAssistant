package com.licencjat.BusinessAssistant.exception;

public class ZoomApiException extends RuntimeException {
    public ZoomApiException(String message) {
        super(message);
    }

    public ZoomApiException(String message, Throwable cause) {
        super(message, cause);
    }
}