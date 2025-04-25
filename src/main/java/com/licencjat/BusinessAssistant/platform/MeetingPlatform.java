package com.licencjat.BusinessAssistant.platform;

/**
 * Główny interfejs platformy spotkań (Zoom, Teams, itp.)
 */
public interface MeetingPlatform {
    /**
     * Zwraca identyfikator platformy
     */
    String getPlatformId();

    /**
     * Tworzy instancję klienta dla danej platformy
     */
    MeetingPlatformClient createClient();
}