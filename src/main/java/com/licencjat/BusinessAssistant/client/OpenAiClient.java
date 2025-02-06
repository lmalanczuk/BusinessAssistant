package com.licencjat.BusinessAssistant.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    public String generateText(String prompt) {
        // TODO: Dodać integrację z OpenAI.
        return "Podsumowanie wygenerowane dla prompta: " + prompt;
    }
}
