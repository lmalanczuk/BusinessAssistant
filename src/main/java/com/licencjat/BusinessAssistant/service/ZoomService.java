package com.licencjat.BusinessAssistant.service;

import com.licencjat.BusinessAssistant.entity.Meeting;

import java.time.LocalDateTime;
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
}
