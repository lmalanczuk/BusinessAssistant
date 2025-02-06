package com.licencjat.BusinessAssistant.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenAiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    public String generateText(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "gpt-3.5-turbo");

            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Jesteś asystentem do podsumowywania spotkań biznesowych");

            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            requestBody.putArray("messages")
                .add(systemMessage)
                .add(userMessage);

            requestBody.put("temperature", 0.5);

            HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody),
                headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            return extractContentFromResponse(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Błąd podczas tworzenia żądania OpenAI", e);
        }
    }

    private String extractContentFromResponse(String jsonResponse) {
        try {
            return objectMapper.readTree(jsonResponse)
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Błąd przetwarzania odpowiedzi OpenAI", e);
        }
    }
}