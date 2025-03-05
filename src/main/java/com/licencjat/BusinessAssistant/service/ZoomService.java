package com.licencjat.BusinessAssistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.licencjat.BusinessAssistant.model.response.ZoomMeetingResponse;
import com.licencjat.BusinessAssistant.model.response.ZoomTokenResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ZoomService {

    /**
     * Przetwarza callback OAuth z Zoom i wymienia kod autoryzacyjny na tokeny
     *
     * @param code Kod autoryzacyjny z Zoom
     * @param userId UUID użytkownika w systemie
     * @return Odpowiedź z tokenami
     */
    ZoomTokenResponse handleOAuthCallback(String code, String userId);

    /**
     * Odświeża token dostępu jeśli jest bliski wygaśnięcia
     *
     * @param userId UUID użytkownika
     * @return Nowe tokeny lub null jeśli odświeżenie nie było potrzebne
     */
    ZoomTokenResponse refreshTokenIfNeeded(UUID userId);

    /**
     * Tworzy spotkanie w Zoom
     *
     * @param userId UUID użytkownika
     * @param title Tytuł spotkania
     * @param startTime Czas rozpoczęcia
     * @param durationMinutes Czas trwania w minutach
     * @return Odpowiedź z danymi utworzonego spotkania
     */
    ZoomMeetingResponse createZoomMeeting(UUID userId, String title, LocalDateTime startTime, int durationMinutes);

    /**
     * Pobiera listę spotkań użytkownika z Zoom
     *
     * @param userId UUID użytkownika
     * @return Dane spotkań w formacie JsonNode
     */
    JsonNode listUserMeetings(UUID userId);

    /**
     * Pobiera nagrania z danego spotkania Zoom
     *
     * @param userId UUID użytkownika
     * @param meetingId ID spotkania w Zoom
     * @return Dane nagrań w formacie JsonNode
     */
    JsonNode getMeetingRecordings(UUID userId, String meetingId);
}