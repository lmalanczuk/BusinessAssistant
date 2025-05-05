//package com.licencjat.BusinessAssistant.client.zoom.impl;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.licencjat.BusinessAssistant.client.zoom.ZoomAuthClient;
//import com.licencjat.BusinessAssistant.client.zoom.ZoomTokenManager;
//import com.licencjat.BusinessAssistant.config.ZoomApiConfig;
//import com.licencjat.BusinessAssistant.exception.AuthenticationException;
//import com.licencjat.BusinessAssistant.model.zoom.ZoomAuthResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//
//@Component
//public class ZoomAuthClientImpl implements ZoomAuthClient {
//    private static final Logger logger = LoggerFactory.getLogger(ZoomAuthClientImpl.class);
//
//    private final ZoomApiConfig apiConfig;
//    private final ObjectMapper objectMapper;
//    private final ZoomTokenManager tokenManager;
//
//    public ZoomAuthClientImpl(ZoomApiConfig apiConfig, ObjectMapper objectMapper, ZoomTokenManager tokenManager) {
//        this.apiConfig = apiConfig;
//        this.objectMapper = objectMapper;
//        this.tokenManager = tokenManager;
//    }
//
//    @Override
//    public ZoomAuthResponse exchangeCodeForTokens(String authorizationCode, String redirectUri) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setBasicAuth(apiConfig.getClientId(), apiConfig.getClientSecret());
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "authorization_code");
//        body.add("code", authorizationCode);
//        body.add("redirect_uri", redirectUri);
//
//        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
//
//        try {
//            logger.info("Exchanging code for tokens with URL: {}", ZoomApiConfig.ZOOM_OAUTH_URL);
//            logger.debug("Request body: {}", body);
//
//            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                ZoomApiConfig.ZOOM_OAUTH_URL,
//                HttpMethod.POST,
//                entity,
//                String.class
//            );
//
//            logger.debug("Token response status: {}", response.getStatusCode());
//            logger.debug("Token response body: {}", response.getBody());
//
//            ZoomAuthResponse authResponse = objectMapper.readValue(response.getBody(), ZoomAuthResponse.class);
//
//            // Aktualizacja tokenów w menedżerze
//            tokenManager.setTokens(
//                authResponse.getAccessToken(),
//                authResponse.getRefreshToken(),
//                authResponse.getExpiresIn()
//            );
//
//            logger.info("Successfully exchanged authorization code for access token");
//            logger.debug("Access token starts with: {}", authResponse.getAccessToken().substring(0, 10) + "...");
//
//            return authResponse;
//        } catch (Exception e) {
//            logger.error("Error exchanging authorization code for tokens: {}", e.getMessage());
//            if (e.getCause() != null) {
//                logger.error("Caused by: {}", e.getCause().getMessage());
//            }
//            throw new AuthenticationException("Failed to exchange authorization code for tokens: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public JsonNode getUserInfo() {
//        try {
//            // Sprawdzenie stanu tokenów przed próbą pobrania danych
//            logger.info("Token state before getting user info: {}", tokenManager.getTokenState());
//
//            // Zapewnienie, że token jest aktualny
//            tokenManager.ensureValidToken();
//
//            HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//
//            String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/users/me";
//            logger.debug("Fetching user info from: {}", url);
//            logger.debug("Headers: {}", headers);
//
//            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                url,
//                HttpMethod.GET,
//                entity,
//                String.class
//            );
//
//            logger.debug("User info response status: {}", response.getStatusCode());
//            logger.debug("User info response body: {}", response.getBody());
//
//            return objectMapper.readTree(response.getBody());
//        } catch (Exception e) {
//            logger.error("Error getting user info from Zoom - {}: {}", e.getClass().getSimpleName(), e.getMessage());
//            if (e.getCause() != null) {
//                logger.error("Caused by: {}: {}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
//            }
//            logger.error("Token state when error occurred: {}", tokenManager.getTokenState());
//            throw new AuthenticationException("Failed to get user info from Zoom: " + e.getMessage(), e);
//        }
//    }
//}