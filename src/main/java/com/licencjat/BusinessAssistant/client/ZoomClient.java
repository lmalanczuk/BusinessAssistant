package com.licencjat.BusinessAssistant.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.config.ZoomConfig;
import com.licencjat.BusinessAssistant.model.response.ZoomTokenResponse;
import com.licencjat.BusinessAssistant.model.request.ZoomMeetingRequest;
import com.licencjat.BusinessAssistant.model.response.ZoomMeetingResponse;
import com.licencjat.BusinessAssistant.model.response.ZoomTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;

@Component
public class ZoomClient {

    private final RestTemplate restTemplate;
    private final ZoomConfig zoomConfig;
    private final ObjectMapper objectMapper;

    private static final String ZOOM_API_BASE_URL = "https://api.zoom.us/v2";
    private static final String ZOOM_OAUTH_TOKEN_URL = "https://zoom.us/oauth/token";

    @Autowired
    public ZoomClient(RestTemplate restTemplate, ZoomConfig zoomConfig) {
        this.restTemplate = restTemplate;
        this.zoomConfig = zoomConfig;
        this.objectMapper = new ObjectMapper();
    }

    public ZoomTokenResponse getAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String credentials = zoomConfig.getClientId() + ":" + zoomConfig.getClientSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        headers.set("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("grant_type", "authorization_code");
        formParams.add("code", authorizationCode);
        formParams.add("redirect_uri", zoomConfig.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        ResponseEntity<ZoomTokenResponse> response = restTemplate.exchange(
            ZOOM_OAUTH_TOKEN_URL,
            HttpMethod.POST,
            entity,
            ZoomTokenResponse.class
        );

        return response.getBody();
    }

    public ZoomTokenResponse refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String credentials = zoomConfig.getClientId() + ":" + zoomConfig.getClientSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        headers.set("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("grant_type", "refresh_token");
        formParams.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        ResponseEntity<ZoomTokenResponse> response = restTemplate.exchange(
            ZOOM_OAUTH_TOKEN_URL,
            HttpMethod.POST,
            entity,
            ZoomTokenResponse.class
        );

        return response.getBody();
    }

    public ZoomMeetingResponse createMeeting(String accessToken, ZoomMeetingRequest meetingRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ZoomMeetingRequest> requestEntity = new HttpEntity<>(meetingRequest, headers);

        ResponseEntity<ZoomMeetingResponse> response = restTemplate.exchange(
            ZOOM_API_BASE_URL + "/users/me/meetings",
            HttpMethod.POST,
            requestEntity,
            ZoomMeetingResponse.class
        );

        return response.getBody();
    }

    public JsonNode listMeetings(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            ZOOM_API_BASE_URL + "/users/me/meetings",
            HttpMethod.GET,
            entity,
            String.class
        );

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Zoom API response", e);
        }
    }

    public JsonNode getMeetingDetails(String accessToken, String meetingId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            ZOOM_API_BASE_URL + "/meetings/" + meetingId,
            HttpMethod.GET,
            entity,
            String.class
        );

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Zoom API response", e);
        }
    }

    public JsonNode getMeetingRecordings(String accessToken, String meetingId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromHttpUrl(ZOOM_API_BASE_URL + "/meetings/" + meetingId + "/recordings")
            .toUriString();

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            String.class
        );

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Zoom API response", e);
        }
    }
}