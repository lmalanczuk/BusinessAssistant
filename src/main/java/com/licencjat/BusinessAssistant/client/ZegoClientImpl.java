package com.licencjat.BusinessAssistant.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ZegoClientImpl implements ZegoClient {
    @Value("${zego.app-id}")
    private long appId;

    @Value("${zego.server-secret}")
    private String serverSecret;

    @Override
    public String generateToken(String userId, String roomId, int privilegeLevel, int effectiveTimeInSeconds) {
    }

    // Inne metody
}
