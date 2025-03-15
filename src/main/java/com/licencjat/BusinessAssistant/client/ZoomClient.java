package com.licencjat.BusinessAssistant.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Http;
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
import java.util.Base64;
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


    public void exchangeCodeForTokens(String authorizationCode, String redirectUri){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(zoomClientId, zoomClientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try{
            ResponseEntity<String> response = restTemplate.exchange(
                ZOOM_OAUTH_URL,
                HttpMethod.POST,
                entity,
                String.class
            );
            processTokenResponse(response.getBody());
            logger.info("Tokens exchanged successfully");
        } catch (Exception e){
            logger.error("Error exchanging code for tokens: {}", e.getMessage());
            throw new RuntimeException("Failed to exchange code for tokens", e);
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

        try{
            ResponseEntity<String> response = restTemplate.exchange(
                ZOOM_OAUTH_URL,
                HttpMethod.POST,
                entity,
                String.class
            );
            processTokenResponse(response.getBody());
            logger.info("Access token refreshed successfully");
        }catch (Exception e){
            logger.error("Error refreshing access token: {}", e.getMessage());
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    public JsonNode getMeeting(String meetingId){
        ensureValidToken();
        HttpHeaders headers = createAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = ZOOM_API_BASE_URL + "/meetings/" + meetingId;

        try{
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            return objectMapper.readTree(response.getBody());
        } catch(Exception e){
            logger.error("Error getting meeting: {}", e.getMessage());
            throw new RuntimeException("Failed to get meeting", e);
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
        if(accessToken == null || tokenExpiryTime.isAfter(tokenExpiryTime.minusSeconds(60))){
            refreshAccessToken();
        }
    }

}
