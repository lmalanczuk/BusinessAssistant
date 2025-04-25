package com.licencjat.BusinessAssistant.exception;

public class SummaryException extends RuntimeException {
    public SummaryException(String message) {
        super(message);
    }

    public SummaryException(String message, Throwable cause) {
        super(message, cause);
    }
}