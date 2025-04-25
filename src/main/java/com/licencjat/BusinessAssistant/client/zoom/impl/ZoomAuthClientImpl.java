package com.licencjat.BusinessAssistant.client.zoom.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.client.zoom.ZoomAuthClient;
import com.licencjat.BusinessAssistant.client.zoom.ZoomTokenManager;
import com.licencjat.BusinessAssistant.config.ZoomApiConfig;
import com.licencjat.BusinessAssistant.exception.AuthenticationException;
import com.licencjat.BusinessAssistant.model.zoom.ZoomAuthResponse;
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

@Component
public class ZoomAuthClientImpl implements ZoomAuthClient {
    private static final Logger logger = LoggerFactory.getLogger(ZoomAuthClientImpl.class);

    private final ZoomApiConfig apiConfig;
    private final ObjectMapper objectMapper;
    private final ZoomTokenManager tokenManager;

    public ZoomAuthClientImpl(ZoomApiConfig apiConfig, ObjectMapper objectMapper, ZoomTokenManager tokenManager) {
        this.apiConfig = apiConfig;
        this.objectMapper = objectMapper;
        this.tokenManager = tokenManager;
    }

    @Override
    public ZoomAuthResponse exchangeCodeForTokens(String authorizationCode, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(apiConfig.getClientId(), apiConfig.getClientSecret());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
                ZoomApiConfig.ZOOM_OAUTH_URL,
                HttpMethod.POST,
                entity,
                String.class
            );

            ZoomAuthResponse authResponse = objectMapper.readValue(response.getBody(), ZoomAuthResponse.class);

            // Aktualizacja tokenów w menedżerze
            tokenManager.setTokens(
                authResponse.getAccessToken(),
                authResponse.getRefreshToken(),
                authResponse.getExpiresIn()
            );

            logger.info("Successfully exchanged authorization code for access token");
            return authResponse;
        } catch (Exception e) {
            logger.error("Error exchanging authorization code for tokens", e);
            throw new AuthenticationException("Failed to exchange authorization code for tokens", e);
        }
    }

    @Override
    public JsonNode getUserInfo() {
        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/users/me";

        try {
            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            logger.error("Error getting user info from Zoom", e);
            throw new AuthenticationException("Failed to get user info from Zoom", e);
        }
    }
}