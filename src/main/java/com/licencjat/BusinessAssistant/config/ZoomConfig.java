package com.licencjat.BusinessAssistant.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Getter
public class ZoomConfig {

    @Value("${zoom.api.client-id}")
    private String clientId;

    @Value("${zoom.api.client-secret}")
    private String clientSecret;

    @Value("${zoom.api.redirect-uri}")
    private String redirectUri;

    @Value("${zoom.api.verification-token}")
    private String verificationToken;

    @Bean
    public RestTemplate zoomRestTemplate() {
        return new RestTemplate();
    }


}