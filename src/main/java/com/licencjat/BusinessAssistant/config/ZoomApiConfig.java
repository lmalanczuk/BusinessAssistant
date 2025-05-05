//package com.licencjat.BusinessAssistant.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestTemplate;
//
//@Configuration
//public class ZoomApiConfig {
//    public static final String ZOOM_API_BASE_URL = "https://api.zoom.us/v2";
//    public static final String ZOOM_OAUTH_URL = "https://zoom.us/oauth/token";
//
//    @Value("${zoom.client-id}")
//    private String clientId;
//
//    @Value("${zoom.client-secret}")
//    private String clientSecret;
//
//    @Value("${zoom.redirect-uri}")
//    private String redirectUri;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public String getClientId() {
//        return clientId;
//    }
//
//    public String getClientSecret() {
//        return clientSecret;
//    }
//
//    public String getRedirectUri() {
//        return redirectUri;
//    }
//
//    public RestTemplate getRestTemplate() {
//        return restTemplate;
//    }
//}