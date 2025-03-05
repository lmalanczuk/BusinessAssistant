package com.licencjat.BusinessAssistant.service;

import com.licencjat.BusinessAssistant.model.request.SummaryRequest;
import com.licencjat.BusinessAssistant.model.response.SummaryResponse;

public interface SummarizationService {

    /**
     * Generuje podsumowanie spotkania na podstawie tekstu transkrypcji.
     *
     * @param request Obiekt zawierający tekst do podsumowania
     * @return Wygenerowane podsumowanie w formie obiektu SummaryResponse
     * @throws IllegalArgumentException jeśli tekst wejściowy jest null lub pusty
     * @throws SummaryGenerationException jeśli wystąpi błąd podczas generowania podsumowania
     */
    SummaryResponse generateSummaryFromText(SummaryRequest request);

    /**
     * Własny wyjątek dla błędów związanych z generowaniem podsumowań
     */
    class SummaryGenerationException extends RuntimeException {
        public SummaryGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}