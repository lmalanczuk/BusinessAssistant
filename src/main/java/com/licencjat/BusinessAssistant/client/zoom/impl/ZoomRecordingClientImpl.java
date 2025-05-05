//package com.licencjat.BusinessAssistant.client.zoom.impl;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.licencjat.BusinessAssistant.client.zoom.ZoomRecordingClient;
//import com.licencjat.BusinessAssistant.client.zoom.ZoomTokenManager;
//import com.licencjat.BusinessAssistant.config.ZoomApiConfig;
//import com.licencjat.BusinessAssistant.exception.ZoomApiException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.util.UriComponentsBuilder;
//
//@Component
//public class ZoomRecordingClientImpl implements ZoomRecordingClient {
//    private static final Logger logger = LoggerFactory.getLogger(ZoomRecordingClientImpl.class);
//
//    private final ZoomApiConfig apiConfig;
//    private final ObjectMapper objectMapper;
//    private final ZoomTokenManager tokenManager;
//
//    public ZoomRecordingClientImpl(ZoomApiConfig apiConfig, ObjectMapper objectMapper, ZoomTokenManager tokenManager) {
//        this.apiConfig = apiConfig;
//        this.objectMapper = objectMapper;
//        this.tokenManager = tokenManager;
//    }
//
//    @Override
//    public JsonNode getUserRecordings(String userId, String from, String to) {
//        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        String url = UriComponentsBuilder.fromHttpUrl(ZoomApiConfig.ZOOM_API_BASE_URL + "/users/" + userId + "/recordings")
//            .queryParam("from", from)
//            .queryParam("to", to)
//            .toUriString();
//
//        try {
//            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                url,
//                HttpMethod.GET,
//                entity,
//                String.class
//            );
//
//            return objectMapper.readTree(response.getBody());
//        } catch (Exception e) {
//            logger.error("Error getting user recordings: {}", e.getMessage());
//            throw new ZoomApiException("Failed to get user recordings", e);
//        }
//    }
//
//    @Override
//    public JsonNode getMeetingRecordings(String meetingId) {
//        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/recordings";
//
//        try {
//            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                url,
//                HttpMethod.GET,
//                entity,
//                String.class
//            );
//            return objectMapper.readTree(response.getBody());
//        } catch (Exception e) {
//            logger.error("Error getting meeting recordings: {}", e.getMessage());
//            throw new ZoomApiException("Failed to get meeting recordings", e);
//        }
//    }
//}