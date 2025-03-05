package com.licencjat.BusinessAssistant.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface ZoomWebhookService {

    /**
     * Obsługuje zdarzenie rozpoczęcia spotkania w Zoom
     *
     * @param webhookData Dane zdarzenia
     */
    void handleMeetingStarted(JsonNode webhookData);

    /**
     * Obsługuje zdarzenie zakończenia spotkania w Zoom
     *
     * @param webhookData Dane zdarzenia
     */
    void handleMeetingEnded(JsonNode webhookData);

    /**
     * Obsługuje zdarzenie zakończenia nagrywania spotkania w Zoom
     *
     * @param webhookData Dane zdarzenia
     */
    void handleRecordingCompleted(JsonNode webhookData);
}