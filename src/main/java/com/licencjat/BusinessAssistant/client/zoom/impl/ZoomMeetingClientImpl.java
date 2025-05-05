//package com.licencjat.BusinessAssistant.client.zoom.impl;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.licencjat.BusinessAssistant.client.zoom.ZoomMeetingClient;
//import com.licencjat.BusinessAssistant.client.zoom.ZoomTokenManager;
//import com.licencjat.BusinessAssistant.config.ZoomApiConfig;
//import com.licencjat.BusinessAssistant.exception.ZoomApiException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class ZoomMeetingClientImpl implements ZoomMeetingClient {
//    private static final Logger logger = LoggerFactory.getLogger(ZoomMeetingClientImpl.class);
//
//    private final ZoomApiConfig apiConfig;
//    private final ObjectMapper objectMapper;
//    private final ZoomTokenManager tokenManager;
//
//    public ZoomMeetingClientImpl(ZoomApiConfig apiConfig, ObjectMapper objectMapper, ZoomTokenManager tokenManager) {
//        this.apiConfig = apiConfig;
//        this.objectMapper = objectMapper;
//        this.tokenManager = tokenManager;
//    }
//
//    @Override
//    public JsonNode getMeeting(String meetingId) {
//        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/meetings/" + meetingId;
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
//            logger.error("Error getting meeting: {}", e.getMessage());
//            throw new ZoomApiException("Failed to get meeting information", e);
//        }
//    }
//
//    @Override
//    public JsonNode createMeeting(Map<String, Object> meetingDetails, String userId) {
//        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        try {
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(meetingDetails, headers);
//            String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/users/" + userId + "/meetings";
//
//            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                url,
//                HttpMethod.POST,
//                entity,
//                String.class
//            );
//            return objectMapper.readTree(response.getBody());
//        } catch (Exception e) {
//            logger.error("Error creating meeting: {}", e.getMessage());
//            throw new ZoomApiException("Failed to create meeting", e);
//        }
//    }
//
//    @Override
//    public JsonNode updateMeeting(String meetingId, Map<String, Object> updateData) {
//        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateData, headers);
//
//        String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/meetings/" + meetingId;
//
//        try {
//            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                url,
//                HttpMethod.PATCH,
//                entity,
//                String.class
//            );
//            return objectMapper.readTree(response.getBody());
//        } catch (Exception e) {
//            logger.error("Error updating meeting: {}", e.getMessage());
//            throw new ZoomApiException("Failed to update meeting", e);
//        }
//    }
//
//    @Override
//    public JsonNode startMeeting(String meetingId) {
//        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, String> requestBody = Map.of("action", "start");
//        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
//
//        String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/status";
//
//        try {
//            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                url,
//                HttpMethod.PUT,
//                entity,
//                String.class
//            );
//            return objectMapper.readTree(response.getBody());
//        } catch (Exception e) {
//            logger.error("Error starting meeting: {}", e.getMessage());
//            throw new ZoomApiException("Failed to start meeting", e);
//        }
//    }
//
//    @Override
//    public JsonNode endMeeting(String meetingId) {
//        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, String> requestBody = Map.of("action", "end");
//        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
//
//        String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/status";
//
//        try {
//            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                url,
//                HttpMethod.PUT,
//                entity,
//                String.class
//            );
//            return objectMapper.readTree(response.getBody());
//        } catch (Exception e) {
//            logger.error("Error ending meeting: {}", e.getMessage());
//            throw new ZoomApiException("Failed to end meeting", e);
//        }
//    }
//
//    @Override
//    public JsonNode getMeetingParticipants(String meetingId) {
//        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/participants";
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
//            logger.error("Error getting meeting participants: {}", e.getMessage());
//            throw new ZoomApiException("Failed to get meeting participants", e);
//        }
//    }
//
//    @Override
//    public JsonNode inviteToMeeting(String meetingId, List<String> emails) {
//        HttpHeaders headers = tokenManager.createAuthenticatedHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("emails", emails);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//
//        String url = ZoomApiConfig.ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/invite";
//
//        try {
//            ResponseEntity<String> response = apiConfig.getRestTemplate().exchange(
//                url,
//                HttpMethod.POST,
//                entity,
//                String.class
//            );
//            return objectMapper.readTree(response.getBody());
//        } catch (Exception e) {
//            logger.error("Error inviting to meeting: {}", e.getMessage());
//            throw new ZoomApiException("Failed to send invitations", e);
//        }
//    }
//}