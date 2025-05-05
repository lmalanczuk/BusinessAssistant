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

    /**
     * Generuje token dla użytkownika
     *
     * @param userId ID użytkownika
     * @param roomId ID pokoju
     * @param privilege Poziom uprawnień (1=tylko subskrypcja, 2=publikowanie)
     * @param effectiveTimeInSeconds Czas ważności tokenu w sekundach
     * @return Wygenerowany token
     */
    public String generateToken(String userId, String roomId, int privilege, int effectiveTimeInSeconds) {
        try {
            return tokenGenerator.generateToken(userId, roomId, privilege, effectiveTimeInSeconds);
        } catch (Exception e) {
            logger.error("Błąd podczas generowania tokenu: {}", e.getMessage());
            throw new ZegoApiException("Nie można wygenerować tokenu", e);
        }
    }
}