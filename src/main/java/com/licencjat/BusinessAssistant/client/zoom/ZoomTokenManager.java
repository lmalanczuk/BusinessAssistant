//package com.licencjat.BusinessAssistant.client.zoom;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.licencjat.BusinessAssistant.config.ZoomApiConfig;
//import com.licencjat.BusinessAssistant.exception.AuthenticationException;
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
//import java.time.Instant;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class ZoomTokenManager {
//    private static final Logger logger = LoggerFactory.getLogger(ZoomTokenManager.class);
//    private static final int REFRESH_THRESHOLD_SECONDS = 300; // 5 minutes before expiry
//    private static final int MAX_RETRY_ATTEMPTS = 3;
//
//    private final ZoomApiConfig apiConfig;
//    private final ObjectMapper objectMapper;
//
//    // Per-user token storage
//    private final Map<UUID, TokenData> userTokens = new ConcurrentHashMap<>();
//
//    // Current user context for backward compatibility
//    private UUID currentUserId;
//
//    public ZoomTokenManager(ZoomApiConfig apiConfig, ObjectMapper objectMapper) {
//        this.apiConfig = apiConfig;
//        this.objectMapper = objectMapper;
//    }
//
//    /**
//     * Stores token data for a specific user
//     */
//    public void setTokensForUser(UUID userId, String accessToken, String refreshToken, long expiresIn) {
//        TokenData tokenData = new TokenData(accessToken, refreshToken, expiresIn);
//        userTokens.put(userId, tokenData);
//        this.currentUserId = userId; // Set current context for backward compatibility
//        logger.info("Tokens set successfully for user: {}", userId);
//    }
//
//    /**
//     * Gets access token for the current user context
//     */
//    public String getAccessToken() {
//        if (currentUserId == null) {
//            throw new AuthenticationException("No current user set for token operations");
//        }
//        return getAccessTokenForUser(currentUserId);
//    }
//
//    /**
//     * Gets access token for a specific user
//     */
//    public String getAccessTokenForUser(UUID userId) {
//        TokenData tokenData = getUserTokenData(userId);
//        ensureValidTokenForUser(userId);
//        return tokenData.accessToken;
//    }
//
//    /**
//     * Sets the current user context
//     */
//    public void setCurrentUser(UUID userId) {
//        if (!userTokens.containsKey(userId)) {
//            throw new AuthenticationException("User " + userId + " has no stored tokens");
//        }
//        this.currentUserId = userId;
//    }
//
//    /**
//     * Refreshes access token for the current user context
//     */
//    public void refreshAccessToken() {
//        if (currentUserId == null) {
//            throw new AuthenticationException("No current user set for token operations");
//        }
//        refreshAccessTokenForUser(currentUserId);
//    }
//
//    /**
//     * Refreshes access token for a specific user
//     */
//    public void refreshAccessTokenForUser(UUID userId) {
//        TokenData tokenData = getUserTokenData(userId);
//
//        if (tokenData.refreshToken == null || tokenData.refreshToken.isEmpty()) {
//            logger.error("No refresh token available for user: {}", userId);
//            userTokens.remove(userId);  // Remove invalid token data
//            throw new AuthenticationException("No refresh token available. User needs to authorize with Zoom again.");
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setBasicAuth(apiConfig.getClientId(), apiConfig.getClientSecret());
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "refresh_token");
//        body.add("refresh_token", tokenData.refreshToken);
//
//        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
//
//        int attempts = 0;
//        Exception lastException = null;
//
//        while (attempts < MAX_RETRY_ATTEMPTS) {
//            try {
//                ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                    ZoomApiConfig.ZOOM_OAUTH_URL,
//                    HttpMethod.POST,
//                    entity,
//                    String.class
//                );
//
//                processTokenResponse(userId, response.getBody());
//                logger.info("Access token refreshed successfully for user: {}", userId);
//                return;
//            } catch (Exception e) {
//                lastException = e;
//                logger.warn("Error refreshing access token (attempt {}/{}): {}",
//                          ++attempts, MAX_RETRY_ATTEMPTS, e.getMessage());
//                if (attempts < MAX_RETRY_ATTEMPTS) {
//                    try {
//                        Thread.sleep(1000 * attempts); // Exponential backoff
//                    } catch (InterruptedException ie) {
//                        Thread.currentThread().interrupt();
//                    }
//                }
//            }
//        }
//
//        // If all attempts failed
//        logger.error("Failed to refresh access token after {} attempts for user: {}",
//                   MAX_RETRY_ATTEMPTS, userId);
//        userTokens.remove(userId);  // Remove invalid token data
//        throw new AuthenticationException("Failed to refresh access token. User needs to reconnect with Zoom.",
//                                       lastException);
//    }
//
//    /**
//     * Ensures valid token for the current user context
//     */
//    public void ensureValidToken() {
//        if (currentUserId == null) {
//            throw new AuthenticationException("No current user set for token operations");
//        }
//        ensureValidTokenForUser(currentUserId);
//    }
//
//    /**
//     * Ensures valid token for a specific user
//     */
//    public void ensureValidTokenForUser(UUID userId) {
//        TokenData tokenData = getUserTokenData(userId);
//
//        if (tokenData.accessToken == null || tokenData.accessToken.isEmpty()) {
//            logger.error("No access token available for user: {}", userId);
//            throw new AuthenticationException("No Zoom access token available. User must authorize with Zoom first.");
//        }
//
//        if (tokenData.expiryTime == null) {
//            logger.error("No token expiry time set for user: {}", userId);
//            refreshAccessTokenForUser(userId);
//            return;
//        }
//
//        // Refresh if token will expire within the threshold
//        if (Instant.now().isAfter(tokenData.expiryTime.minusSeconds(REFRESH_THRESHOLD_SECONDS))) {
//            logger.info("Token expired or about to expire for user: {} (expires at: {}), refreshing",
//                      userId, tokenData.expiryTime);
//            refreshAccessTokenForUser(userId);
//        }
//    }
//
//    /**
//     * Creates authenticated headers for API requests
//     */
//    public HttpHeaders createAuthenticatedHeaders() {
//        if (currentUserId == null) {
//            throw new AuthenticationException("No current user set for token operations");
//        }
//        return createAuthenticatedHeadersForUser(currentUserId);
//    }
//
//    /**
//     * Creates authenticated headers for a specific user
//     */
//    public HttpHeaders createAuthenticatedHeadersForUser(UUID userId) {
//        logger.debug("Creating authenticated headers for user: {}", userId);
//
//        ensureValidTokenForUser(userId);
//        TokenData tokenData = userTokens.get(userId);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + tokenData.accessToken);
//
//        return headers;
//    }
//
//    /**
//     * Gets refresh token for current user context
//     */
//    public String getRefreshToken() {
//        if (currentUserId == null) {
//            throw new AuthenticationException("No current user set for token operations");
//        }
//        return userTokens.get(currentUserId).refreshToken;
//    }
//
//    /**
//     * Gets token state for current user context
//     */
//    public String getTokenState() {
//        if (currentUserId == null) {
//            return "No current user set";
//        }
//
//        TokenData tokenData = userTokens.get(currentUserId);
//        if (tokenData == null) {
//            return "No tokens available for current user";
//        }
//
//        return String.format("User: %s, AccessToken: %s, RefreshToken: %s, Expiry: %s",
//                           currentUserId,
//                           tokenData.accessToken != null ? "set" : "null",
//                           tokenData.refreshToken != null ? "set" : "null",
//                           tokenData.expiryTime != null ? tokenData.expiryTime.toString() : "null");
//    }
//
//    /**
//     * Processes token response from OAuth or refresh
//     */
//    private void processTokenResponse(UUID userId, String responseBody) {
//        try {
//            JsonNode responseJson = objectMapper.readTree(responseBody);
//            String accessToken = responseJson.get("access_token").asText();
//            String refreshToken = responseJson.get("refresh_token").asText();
//            int expiresIn = responseJson.get("expires_in").asInt();
//
//            TokenData tokenData = new TokenData(accessToken, refreshToken, expiresIn);
//            userTokens.put(userId, tokenData);
//
//            logger.debug("Token processed for user: {}, new expiry time: {}", userId, tokenData.expiryTime);
//        } catch (Exception e) {
//            logger.error("Failed to process token response for user {}: {}", userId, e.getMessage());
//            throw new AuthenticationException("Failed to process token response", e);
//        }
//    }
//
//    /**
//     * Gets token data for a specific user, throwing exception if not found
//     */
//    private TokenData getUserTokenData(UUID userId) {
//        TokenData tokenData = userTokens.get(userId);
//        if (tokenData == null) {
//            logger.error("No token data found for user: {}", userId);
//            throw new AuthenticationException("User " + userId + " has no Zoom authorization");
//        }
//        return tokenData;
//    }
//
//    /**
//     * Gets all valid users with tokens
//     */
//    public Set<UUID> getAllAuthorizedUsers() {
//        return userTokens.keySet();
//    }
//
//    /**
//     * Removes token data for a specific user
//     */
//    public void removeTokensForUser(UUID userId) {
//        userTokens.remove(userId);
//        if (currentUserId != null && currentUserId.equals(userId)) {
//            currentUserId = null;
//        }
//        logger.info("Tokens removed for user: {}", userId);
//    }
//
//    /**
//     * Internal class to store token data
//     */
//    private static class TokenData {
//        private final String accessToken;
//        private final String refreshToken;
//        private final Instant expiryTime;
//
//        public TokenData(String accessToken, String refreshToken, long expiresIn) {
//            this.accessToken = accessToken;
//            this.refreshToken = refreshToken;
//            this.expiryTime = Instant.now().plusSeconds(expiresIn);
//        }
//    }
//}