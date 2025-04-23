package com.licencjat.BusinessAssistant.exception;

public class RecordingException extends RuntimeException {
    public RecordingException(String message) {
        super(message);
    }

    public RecordingException(String message, Throwable cause) {
        super(message, cause);
    }
}