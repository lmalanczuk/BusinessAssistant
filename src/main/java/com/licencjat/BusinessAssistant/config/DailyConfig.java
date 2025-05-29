package com.licencjat.BusinessAssistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DailyConfig {
    
    @Value("${daily.api.key}")
    private String apiKey;
    
    @Value("${daily.api.url:https://api.daily.co/v1}")
    private String apiUrl;
    
    @Value("${daily.domain}")
    private String domain; // np. "your-subdomain" dla your-subdomain.daily.co
    
    @Bean
    public RestTemplate dailyRestTemplate() {
        return new RestTemplate();
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public String getRoomUrl(String roomName) {
        return String.format("https://%s.daily.co/%s", domain, roomName);
    }
}