package com.licencjat.BusinessAssistant.client;

public interface ZegoClient {
    String generateToken(String userId, String roomId, int privilegeLevel, int effectiveTimeInSeconds);
}
