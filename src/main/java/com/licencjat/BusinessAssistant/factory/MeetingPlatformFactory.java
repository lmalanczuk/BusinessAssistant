package com.licencjat.BusinessAssistant.factory;

import com.licencjat.BusinessAssistant.platform.MeetingPlatform;
import com.licencjat.BusinessAssistant.platform.MeetingPlatformClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fabryka odpowiedzialna za dostarczanie odpowiednich platform
 */
@Component
public class MeetingPlatformFactory {
    private static final Logger logger = LoggerFactory.getLogger(MeetingPlatformFactory.class);
    private final Map<String, MeetingPlatform> platforms = new HashMap<>();

    @Autowired
    public MeetingPlatformFactory(List<MeetingPlatform> platformList) {
        for (MeetingPlatform platform : platformList) {
            platforms.put(platform.getPlatformId(), platform);
            logger.info("Zarejestrowano platformę spotkań: {}", platform.getPlatformId());
        }
    }

    /**
     * Zwraca klienta dla określonej platformy
     */
    public MeetingPlatformClient getClient(String platformId) {
        MeetingPlatform platform = platforms.get(platformId);
        if (platform == null) {
            throw new IllegalArgumentException("Nieznana platforma spotkań: " + platformId);
        }
        return platform.createClient();
    }

    /**
     * Sprawdza czy platforma jest obsługiwana
     */
    public boolean isPlatformSupported(String platformId) {
        return platforms.containsKey(platformId);
    }

    /**
     * Zwraca listę dostępnych platform
     */
    public List<String> getSupportedPlatforms() {
        return List.copyOf(platforms.keySet());
    }
}