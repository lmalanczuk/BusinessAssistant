package com.licencjat.BusinessAssistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.licencjat.BusinessAssistant.entity.Meeting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ZoomService {
    /**
     * Przetwarza webhook z zooma
     * @param webhookData
     */
    void processWebhook(Map<String, Object> webhookData);

    /**
     * Tworzy spotkanie i zapisuje w bazie danych
     * @param title
     * @param startTime
     * @param durationMinutes
     * @param hostUserId
     * @return
     */
    Meeting createZoomMeeting(String title, LocalDateTime startTime, int durationMinutes, UUID hostUserId);

    /**
     * Pobiera nagrania dla okreslonego spotkania
     * @param meetingId
     */
    void downloadRecordingsForMeeting(String meetingId);

    /**
     * Rozpoczyna spotkanie Zoom
     * @param meetingId ID spotkania do rozpoczęcia
     * @return Zaktualizowane spotkanie
     */
    Meeting startMeeting(String meetingId);

    /**
     * Kończy spotkanie Zoom
     * @param meetingId ID spotkania do zakończenia
     * @return Zaktualizowane spotkanie
     */
    Meeting endMeeting(String meetingId);

    /**
     * Pobiera listę uczestników spotkania
     * @param meetingId ID spotkania
     * @return Dane uczestników w formacie JsonNode
     */
    JsonNode getMeetingParticipants(String meetingId);

    /**
     * Wysyła zaproszenie do spotkania Zoom
     * @param meetingId ID spotkania
     * @param emails Lista adresów email uczestników
     * @return Status operacji
     */
    JsonNode inviteToMeeting(String meetingId, List<String> emails);

    /**
     * Aktualizuje istniejące spotkanie Zoom
     * @param meetingId ID spotkania do aktualizacji
     * @param updateData Mapa z danymi do aktualizacji
     * @return Zaktualizowane spotkanie
     */
    Meeting updateMeeting(String meetingId, Map<String, Object> updateData);

     /**
     * Pobiera szczegóły spotkania
     * @param meetingId ID spotkania
     * @return Szczegóły spotkania w formacie JsonNode
     */
    JsonNode getMeeting(String meetingId);

    /**
     * Pobiera nagrania dla spotkania
     * @param meetingId ID spotkania
     * @return Nagrania spotkania w formacie JsonNode
     */
    JsonNode getMeetingRecordings(String meetingId);

    /**
     * Pobiera nagrania użytkownika z określonego okresu
     * @param userId ID użytkownika Zoom
     * @param from Data początkowa (format ISO)
     * @param to Data końcowa (format ISO)
     * @return Nagrania użytkownika w formacie JsonNode
     */
    JsonNode getUserRecordings(String userId, String from, String to);
}