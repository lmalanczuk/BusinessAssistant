package com.licencjat.BusinessAssistant.client.zoom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.config.ZoomApiConfig;
import com.licencjat.BusinessAssistant.exception.AuthenticationException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

@Component
public class ZoomTokenManager {
    private static final Logger logger = LoggerFactory.getLogger(ZoomTokenManager.class);

    private final ZoomApiConfig apiConfig;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiryTime;

    public ZoomTokenManager(ZoomApiConfig apiConfig, ObjectMapper objectMapper) {
        this.apiConfig = apiConfig;
        this.objectMapper = objectMapper;
    }

    public String getAccessToken() {
        ensureValidToken();
        return accessToken;
    }

    public void setTokens(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiryTime = Instant.now().plusSeconds(expiresIn);
        logger.info("Tokens set, expires at: {}", tokenExpiryTime);
    }

    public void refreshAccessToken() {
        if (refreshToken == null) {
            logger.error("No refresh token available for refreshing access token");
            throw new AuthenticationException("No refresh token available. User needs to authorize with Zoom again.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(apiConfig.getClientId(), apiConfig.getClientSecret());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
                ZoomApiConfig.ZOOM_OAUTH_URL,
                HttpMethod.POST,
                entity,
                String.class
            );

            processTokenResponse(response.getBody());
            logger.info("Access token refreshed successfully");
        } catch (Exception e) {
            logger.error("Error refreshing access token: {}", e.getMessage());
            // Reset tokens to force reauthentication
            this.accessToken = null;
            this.refreshToken = null;
            this.tokenExpiryTime = null;
            throw new AuthenticationException("Failed to refresh access token. User needs to reconnect with Zoom.", e);
        }
    }

    public void ensureValidToken() {
        if (accessToken == null) {
            logger.warn("No access token available");
            throw new AuthenticationException("No Zoom access token available. User must authorize with Zoom first.");
        }

        if (tokenExpiryTime == null || Instant.now().isAfter(tokenExpiryTime.minusSeconds(60))) {
            logger.info("Token expired or about to expire, refreshing");
            refreshAccessToken();
        }
    }

    private void processTokenResponse(String responseBody) {
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            accessToken = responseJson.get("access_token").asText();
            refreshToken = responseJson.get("refresh_token").asText();
            int expiresIn = responseJson.get("expires_in").asInt();
            tokenExpiryTime = Instant.now().plusSeconds(expiresIn);
            logger.debug("Token processed, new expiry time: {}", tokenExpiryTime);
        } catch (Exception e) {
            logger.error("Failed to process token response: {}", e.getMessage());
            throw new AuthenticationException("Failed to process token response", e);
        }
    }

    public HttpHeaders createAuthenticatedHeaders() {
        ensureValidToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    /**
    * Gets the current refresh token
    * @return The current refresh token or null if not set
     */
    public String getRefreshToken() {
     return refreshToken;
    }
}