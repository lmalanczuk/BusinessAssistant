package com.licencjat.BusinessAssistant.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.exception.AuthenticationException;
import com.licencjat.BusinessAssistant.exception.ZoomApiException;
import com.licencjat.BusinessAssistant.model.zoom.ZoomAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ZoomClient {
    private static final Logger logger = LoggerFactory.getLogger(ZoomClient.class);
    private static final String ZOOM_API_BASE_URL = "https://api.zoom.us/v2";
    private static final String ZOOM_OAUTH_URL = "https://zoom.us/oauth/token";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String refreshToken;
    private String accessToken;
    private Instant tokenExpiryTime;

    @Value("${zoom.client-id}")
    private String zoomClientId;

    @Value("${zoom.client-secret}")
    private String zoomClientSecret;

    public ZoomClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }


    public ZoomAuthResponse exchangeCodeForTokens(String authorizationCode, String redirectUri) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(zoomClientId, zoomClientSecret);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("code", authorizationCode);
    body.add("redirect_uri", redirectUri);

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

    try {
        ResponseEntity<String> response = restTemplate.exchange(
                ZOOM_OAUTH_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        ZoomAuthResponse authResponse = objectMapper.readValue(response.getBody(), ZoomAuthResponse.class);
        this.accessToken = authResponse.getAccessToken();
        this.refreshToken = authResponse.getRefreshToken();
        this.tokenExpiryTime = Instant.now().plusSeconds(authResponse.getExpiresIn());

        logger.info("Successfully exchanged authorization code for access token");
        return authResponse;
    } catch (Exception e) {
        logger.error("Error exchanging authorization code for tokens", e);
        throw new RuntimeException("Failed to exchange authorization code for tokens", e);
    }
}

    public void refreshAccessToken() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(zoomClientId, zoomClientSecret);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "refresh_token");
    body.add("refresh_token", refreshToken);

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

    try {
        ResponseEntity<String> response = restTemplate.exchange(
            ZOOM_OAUTH_URL,
            HttpMethod.POST,
            entity,
            String.class
        );
        processTokenResponse(response.getBody());
        logger.info("Access token refreshed successfully");
    } catch (Exception e) {
        logger.error("Error refreshing access token: {}", e.getMessage());
        throw new AuthenticationException("Failed to refresh access token", e);
    }
}

    public JsonNode getMeeting(String meetingId) {
    ensureValidToken();
    HttpHeaders headers = createAuthenticatedHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    String url = ZOOM_API_BASE_URL + "/meetings/" + meetingId;

    try {
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            String.class
        );
        return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
        logger.error("Error getting meeting: {}", e.getMessage());
        throw new ZoomApiException("Failed to get meeting information", e);
    }
}

    public JsonNode createMeeting(Map<String, Object> meetingDetails, String userId) {
        ensureValidToken();

        HttpHeaders headers = createAuthenticatedHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
        String requestBody = objectMapper.writeValueAsString(meetingDetails);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(meetingDetails, headers);
        String url = ZOOM_API_BASE_URL + "/users/" + userId + "/meetings";

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return objectMapper.readTree(response.getBody());
        } catch (Exception e){
            logger.error("Error creating meeting: {}", e.getMessage());
            throw new RuntimeException("Failed to create meeting", e);
        }
    }

    public JsonNode getUserRecordings(String userId, String from, String to){
        ensureValidToken();

        HttpHeaders headers = createAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromHttpUrl(ZOOM_API_BASE_URL + "/users/" + userId + "/recordings")
            .queryParam("from", from)
            .queryParam("to", to)
            .toUriString();

        try{
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            return objectMapper.readTree(response.getBody());
        } catch (Exception e){
            logger.error("Error getting user recordings: {}", e.getMessage());
            throw new RuntimeException("Failed to get user recordings", e);
        }
    }

    public JsonNode getMeetingRecordings(String meetingId){
        ensureValidToken();

        HttpHeaders headers = createAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/recordings";

        try{
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            return objectMapper.readTree(response.getBody());
    } catch (Exception e){
        logger.error("Error getting meeting recordings: {}", e.getMessage());
        throw new RuntimeException("Failed to get meeting recordings", e);
    }
    }

    /**
     * Metody pomocnnicze
     */
    private void processTokenResponse(String responseBody) throws JsonProcessingException{
        JsonNode responseJson = objectMapper.readTree(responseBody);
        accessToken = responseJson.get("access_token").asText();
        refreshToken = responseJson.get("refresh_token").asText();
        int expiresIn = responseJson.get("expires_in").asInt();
        tokenExpiryTime = Instant.now().plusSeconds(expiresIn);

    }

    private HttpHeaders createAuthenticatedHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private void ensureValidToken(){
        if(accessToken == null || Instant.now().isAfter(tokenExpiryTime.minusSeconds(60))){
    refreshAccessToken();
}
    }

    /**
 * Pobiera informacje o użytkowniku Zoom
 */
public JsonNode getUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    String url = ZOOM_API_BASE_URL + "/users/me";

    try {
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );
        return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
        logger.error("Błąd podczas pobierania informacji o użytkowniku Zoom", e);
        throw new RuntimeException("Nie udało się pobrać informacji o użytkowniku Zoom", e);
    }
}
/**
 * Rozpoczyna spotkanie Zoom
 * @param meetingId ID spotkania
 * @return JsonNode z odpowiedzią API
 */
public JsonNode startMeeting(String meetingId) {
    ensureValidToken();
    HttpHeaders headers = createAuthenticatedHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> requestBody = Map.of("action", "start");
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

    String url = ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/status";

    try {
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            entity,
            String.class
        );
        return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
        logger.error("Error starting meeting: {}", e.getMessage());
        throw new RuntimeException("Failed to start meeting", e);
    }
}

/**
 * Kończy spotkanie Zoom
 * @param meetingId ID spotkania
 * @return JsonNode z odpowiedzią API
 */
public JsonNode endMeeting(String meetingId) {
    ensureValidToken();
    HttpHeaders headers = createAuthenticatedHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> requestBody = Map.of("action", "end");
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

    String url = ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/status";

    try {
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            entity,
            String.class
        );
        return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
        logger.error("Error ending meeting: {}", e.getMessage());
        throw new RuntimeException("Failed to end meeting", e);
    }
}

/**
 * Pobiera uczestników spotkania
 * @param meetingId ID spotkania
 * @return JsonNode z listą uczestników
 */
public JsonNode getMeetingParticipants(String meetingId) {
    ensureValidToken();
    HttpHeaders headers = createAuthenticatedHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    String url = ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/participants";

    try {
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            String.class
        );
        return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
        logger.error("Error getting meeting participants: {}", e.getMessage());
        throw new RuntimeException("Failed to get meeting participants", e);
    }
}

/**
 * Wysyła zaproszenia do spotkania Zoom
 * @param meetingId ID spotkania
 * @param emails Lista adresów email
 * @return JsonNode z odpowiedzią API
 */
public JsonNode inviteToMeeting(String meetingId, List<String> emails) {
    ensureValidToken();
    HttpHeaders headers = createAuthenticatedHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("emails", emails);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    String url = ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/invite";

    try {
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String.class
        );
        return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
        logger.error("Error inviting to meeting: {}", e.getMessage());
        throw new RuntimeException("Failed to send invitations", e);
    }
}

/**
 * Aktualizuje istniejące spotkanie
 * @param meetingId ID spotkania
 * @param updateData Mapa z danymi do aktualizacji
 * @return JsonNode z zaktualizowanym spotkaniem
 */
public JsonNode updateMeeting(String meetingId, Map<String, Object> updateData) {
    ensureValidToken();
    HttpHeaders headers = createAuthenticatedHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateData, headers);

    String url = ZOOM_API_BASE_URL + "/meetings/" + meetingId;

    try {
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.PATCH,
            entity,
            String.class
        );
        return objectMapper.readTree(response.getBody());
    } catch (Exception e) {
        logger.error("Error updating meeting: {}", e.getMessage());
        throw new RuntimeException("Failed to update meeting", e);
    }
}
}
