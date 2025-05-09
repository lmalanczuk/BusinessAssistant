package com.licencjat.BusinessAssistant.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.config.ZegoConfig;
import com.licencjat.BusinessAssistant.util.ZegoTokenGenerator;
import com.licencjat.BusinessAssistant.exception.ZegoApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZegoClient {
    private static final Logger logger = LoggerFactory.getLogger(ZegoClient.class);

    private final ZegoConfig zegoConfig;
    private final ZegoTokenGenerator tokenGenerator;
    private final ObjectMapper objectMapper;

    @Autowired
    public ZegoClient(ZegoConfig zegoConfig, ZegoTokenGenerator tokenGenerator, ObjectMapper objectMapper) {
        this.zegoConfig = zegoConfig;
        this.tokenGenerator = tokenGenerator;
        this.objectMapper = objectMapper;
    }
}