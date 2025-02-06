package com.licencjat.BusinessAssistant.service.impl;

import com.licencjat.BusinessAssistant.client.OpenAiClient;
import com.licencjat.BusinessAssistant.model.request.SummaryRequest;
import com.licencjat.BusinessAssistant.model.response.SummaryResponse;
import com.licencjat.BusinessAssistant.service.SummarizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SummarizationServiceImpl implements SummarizationService {

    private static final Logger logger = LoggerFactory.getLogger(SummarizationServiceImpl.class);
    private static final int MAX_INPUT_LENGTH = 12000;
    private static final int MAX_SUMMARY_LENGTH = 2000;

    private final OpenAiClient openAiClient;

    @Autowired
    public SummarizationServiceImpl(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    @Override
public SummaryResponse generateSummaryFromText(SummaryRequest request) {
    try {
        String inputText = validateAndPrepareInput(request.getText());
        String structuredPrompt = createStructuredPrompt(inputText);

        logger.info("Generating summary for text length: {}", inputText.length());
        String rawSummary = openAiClient.generateText(structuredPrompt);

        // Formatowanie odpowiedzi
        String formattedSummary = formatSummaryResponse(rawSummary);

        // Wypisanie sformatowanego tekstu (logowanie)
        logger.info("Formatted Summary:\n{}", formattedSummary);

        return new SummaryResponse(formattedSummary);
    } catch (Exception e) {
        logger.error("Error generating summary: {}", e.getMessage());
        throw new SummaryGenerationException("Failed to generate summary: " + e.getMessage(), e);
    }
}

private String formatMarkdownSummary(String rawSummary) {
    // 1. Normalizacja nagłówków
    rawSummary = rawSummary.replaceAll("(?m)^#{1,6}\\s*", "### ");

    // 2. Poprawa formatowania list
    rawSummary = rawSummary.replaceAll("(?m)^\\s*\\d+\\.", "1.") // Normalizacja numeracji
                          .replaceAll("(?m)^\\s*-", "-")        // Normalizacja punktów
                          .replaceAll("(?m)^\\s*\\*", "-");     // Zamiana gwiazdek na myślniki

    // 3. Usuwanie nadmiarowych pustych linii
    rawSummary = rawSummary.replaceAll("(?m)^\\s*$[\n\r]{1,}", "");

    // 4. Poprawa formatowania tabel
    rawSummary = rawSummary.replaceAll("\\|\\s*\\n", "|\n") // Usuwanie spacji po pionowych kreskach
                          .replaceAll("\\n\\s*\\|", "\n|"); // Usuwanie spacji przed pionowymi kreskami

    // 5. Dodanie spacji po nagłówkach
    rawSummary = rawSummary.replaceAll("(?m)^(#{1,6} .+)$", "$1\n");

    // 6. Usuwanie nadmiarowych znaków nowej linii
    rawSummary = rawSummary.trim();

    return rawSummary;
}

    private String validateAndPrepareInput(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty");
        }

        // Normalizacja tekstu
        String cleanedText = text.replaceAll("\\s+", " ").trim();

        // Obcinanie zbyt długich tekstów
        if (cleanedText.length() > MAX_INPUT_LENGTH) {
            logger.warn("Input text truncated from {} to {} characters",
                cleanedText.length(), MAX_INPUT_LENGTH);
            return cleanedText.substring(0, MAX_INPUT_LENGTH) + "... [TEXT TRUNCATED]";
        }

        return cleanedText;
    }

   private String createStructuredPrompt(String inputText) {
    return String.format("""
        Stwórz profesjonalne podsumowanie spotkania biznesowego w formie punktowanej listy.
        Użyj następującej struktury:

        ### Kluczowe tematy
        - lista najważniejszych tematów

        ### Podjęte decyzje
        - decyzja 1
        - decyzja 2

        ### Zadania do wykonania
        - zadanie 1
        - zadanie 2

        ### Wykryte problemy/ryzyka
        - problem 1
        - problem 2

        ### Następne kroki
        - krok 1
        - krok 2

        Tekst transkrypcji:
        %s

        UWAGA:
        - Używaj wyłącznie języka polskiego
        - Zachowaj formalny ton biznesowy
        - Unikaj technicznego żargonu
        - Nie używaj tabel
        - Wszystkie sekcje mają być w formie punktowanej listy
        - Maksymalna długość podsumowania: %d znaków
        - Response powinien być zgodny z formatem MarkDown
        - W przypadku braku decyzji, zadań, problemów lub kroków, pozostaw odpowiednią sekcję pustą
        """, inputText, MAX_SUMMARY_LENGTH);
}

    private SummaryResponse processSummaryOutput(String rawSummary) {
        // Dodatkowe przetwarzanie wyników
        String cleanedSummary = rawSummary
            .replaceAll("(?m)^\\s*$[\n\r]{1,}", "") // Usuwanie pustych linii
            .trim();

        // Walidacja długości
        if (cleanedSummary.length() > MAX_SUMMARY_LENGTH) {
            logger.warn("Generated summary exceeded limit: {} characters", cleanedSummary.length());
            cleanedSummary = cleanedSummary.substring(0, MAX_SUMMARY_LENGTH) + "... [SUMMARY TRUNCATED]";
        }

        return new SummaryResponse(cleanedSummary);
    }
    private String formatSummaryResponse(String rawSummary) {
    // 1. Zamiana \n na rzeczywiste znaki nowej linii
    String formattedSummary = rawSummary.replace("\\n", System.lineSeparator());

    // 2. Usuwanie podwójnych spacji
    formattedSummary = formattedSummary.replaceAll("\\s{2,}", " ");

    // 3. Usuwanie nadmiarowych pustych linii
    formattedSummary = formattedSummary.replaceAll("(?m)^\\s*$[\n\r]{1,}", "");

    // 4. Przycinanie tekstu
    formattedSummary = formattedSummary.trim();

    return formattedSummary;
}

    public static class SummaryGenerationException extends RuntimeException {
        public SummaryGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}