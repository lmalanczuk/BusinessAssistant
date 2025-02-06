package com.licencjat.BusinessAssistant.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AIMicroserviceClient {
    /**
     *     Klasa testowa. W przyszłości zmienią się adresy URL, metody etc.
     */
    private static final String TRANSCRIBE_URL = "http://127.0.0.1:8000/transcribe";
    private static final String SUMMARIZE_URL = "http://127.0.0.1:8000/summarize";
    private static final String ANALYZE_URL = "http://127.0.0.1:8000/analyze";
    private final RestTemplate restTemplate;

    public AIMicroserviceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void callTranscribeEndpoint() {

            String transcribe_text = "This is a test text";
            String transcribeRequest = "{\"text\": \"" + transcribe_text + "\"}";
            ResponseEntity<String> transcribeResponse = restTemplate.postForEntity(TRANSCRIBE_URL, createHttpEntity(transcribeRequest), String.class);
            System.out.println("Transcribe Response: " + transcribeResponse.getBody());



    }
 private static HttpEntity<String> createHttpEntity(String json) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new HttpEntity<>(json, headers);
        }
}
