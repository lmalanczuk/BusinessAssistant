package com.licencjat.BusinessAssistant.exception;

public class ZegoApiException extends RuntimeException {
    public ZegoApiException(String message) {
        super(message);
    }

    public ZegoApiException(String message, Throwable cause) {
        super(message, cause);
    }
}