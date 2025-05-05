package com.licencjat.BusinessAssistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ZegoConfig {

    @Value("${zego.app-id}")
    private long appId;

    @Value("${zego.server-secret}")
    private String serverSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public long getAppId() {
        return appId;
    }

    public String getServerSecret() {
        return serverSecret;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}