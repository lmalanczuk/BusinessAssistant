package com.licencjat.BusinessAssistant.platform;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Wspólny interfejs dla klientów platform spotkań
 */
public interface MeetingPlatformClient {
    /**
     * Tworzy nowe spotkanie na platformie
     */
    JsonNode createMeeting(String title, LocalDateTime startTime, int durationMinutes,
                           String userId, Map<String, Object> options);

    /**
     * Pobiera szczegóły spotkania
     */
    JsonNode getMeeting(String meetingId);

    /**
     * Aktualizuje spotkanie
     */
    JsonNode updateMeeting(String meetingId, Map<String, Object> updateData);

    /**
     * Rozpoczyna spotkanie
     */
    JsonNode startMeeting(String meetingId);

    /**
     * Kończy spotkanie
     */
    JsonNode endMeeting(String meetingId);

    /**
     * Pobiera listę uczestników spotkania
     */
    JsonNode getMeetingParticipants(String meetingId);

    /**
     * Wysyła zaproszenia do spotkania
     */
    JsonNode inviteToMeeting(String meetingId, List<String> emails);

    /**
     * Pobiera nagrania spotkania
     */
    JsonNode getMeetingRecordings(String meetingId);
}