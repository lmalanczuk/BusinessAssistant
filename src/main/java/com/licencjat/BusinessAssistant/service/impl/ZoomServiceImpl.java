package com.licencjat.BusinessAssistant.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.entity.enums.Platform;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import com.licencjat.BusinessAssistant.exception.ResourceNotFoundException;
import com.licencjat.BusinessAssistant.exception.ZoomApiException;
import com.licencjat.BusinessAssistant.factory.MeetingPlatformFactory;
import com.licencjat.BusinessAssistant.model.zoom.webhook.ZoomWebhookEvent;
import com.licencjat.BusinessAssistant.platform.MeetingPlatformClient;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.service.WebhookHandlerRegistry;
import com.licencjat.BusinessAssistant.service.ZoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ZoomServiceImpl implements ZoomService {
    private static final Logger logger = LoggerFactory.getLogger(ZoomServiceImpl.class);

    private final MeetingPlatformFactory platformFactory;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final WebhookHandlerRegistry webhookHandlerRegistry;

    @Autowired
    public ZoomServiceImpl(
            MeetingPlatformFactory platformFactory,
            MeetingRepository meetingRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            WebhookHandlerRegistry webhookHandlerRegistry) {
        this.platformFactory = platformFactory;
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.webhookHandlerRegistry = webhookHandlerRegistry;
    }

    @Override
    public void processWebhook(Map<String, Object> webhookData) {
        try {
            // Wyodrębnienie typu wydarzenia
            ZoomWebhookEvent event = objectMapper.convertValue(webhookData, ZoomWebhookEvent.class);
            String eventType = event.getEvent();

            // Delegacja do rejestru handlerów
            webhookHandlerRegistry.processWebhook(eventType, webhookData);
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            throw new RuntimeException("Błąd przetwarzania webhooka", e);
        }
    }

    @Override
    public void downloadRecordingsForMeeting(String meetingId) {
        // Implementacja zostanie dodana w przyszłości
        throw new UnsupportedOperationException("Metoda jeszcze nie zaimplementowana");
    }

    @Override
    @Transactional
    public Meeting startMeeting(String meetingId) {
        try {
            // Znajdź spotkanie w bazie danych
            Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Nie znaleziono spotkania o ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Pobierz klienta platformy
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Wywołaj API platformy do rozpoczęcia spotkania
            platformClient.startMeeting(meetingId);

            // Aktualizuj status spotkania w bazie danych
            meeting.setStatus(Status.ONGOING);
            meeting.setStartTime(LocalDateTime.now());

            return meetingRepository.save(meeting);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Błąd podczas rozpoczynania spotkania: {}", e.getMessage());
            throw new ZoomApiException("Nie można rozpocząć spotkania", e);
        }
    }

    @Override
    @Transactional
    public Meeting endMeeting(String meetingId) {
        try {
            // Znajdź spotkanie w bazie danych
            Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new IllegalArgumentException("Nie znaleziono spotkania o ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Pobierz klienta platformy
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Wywołaj API platformy do zakończenia spotkania
            platformClient.endMeeting(meetingId);

            // Aktualizuj status spotkania w bazie danych
            meeting.setStatus(Status.COMPLETED);
            meeting.setEndTime(LocalDateTime.now());

            return meetingRepository.save(meeting);
        } catch (Exception e) {
            logger.error("Błąd podczas kończenia spotkania: {}", e.getMessage());
            throw new RuntimeException("Nie można zakończyć spotkania", e);
        }
    }

    @Override
    public JsonNode getMeetingParticipants(String meetingId) {
        try {
            // Sprawdź, czy spotkanie istnieje w bazie danych
            Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new IllegalArgumentException("Nie znaleziono spotkania o ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Pobierz klienta platformy
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Wywołaj API platformy do pobrania uczestników
            return platformClient.getMeetingParticipants(meetingId);
        } catch (Exception e) {
            logger.error("Błąd podczas pobierania uczestników spotkania: {}", e.getMessage());
            throw new RuntimeException("Nie można pobrać uczestników spotkania", e);
        }
    }

    @Override
    public JsonNode inviteToMeeting(String meetingId, List<String> emails) {
        try {
            // Sprawdź, czy spotkanie istnieje w bazie danych
            Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new IllegalArgumentException("Nie znaleziono spotkania o ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Pobierz klienta platformy
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Wywołaj API platformy do wysłania zaproszeń
            return platformClient.inviteToMeeting(meetingId, emails);
        } catch (Exception e) {
            logger.error("Błąd podczas wysyłania zaproszeń: {}", e.getMessage());
            throw new RuntimeException("Nie można wysłać zaproszeń", e);
        }
    }

    @Override
    @Transactional
    public Meeting updateMeeting(String meetingId, Map<String, Object> updateData) {
        try {
            // Znajdź spotkanie w bazie danych
            Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new IllegalArgumentException("Nie znaleziono spotkania o ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Pobierz klienta platformy
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Wywołaj API platformy do aktualizacji spotkania
            JsonNode updatedMeetingData = platformClient.updateMeeting(meetingId, updateData);

            // Aktualizuj dane spotkania w bazie danych
            if (updateData.containsKey("topic")) {
                meeting.setTitle((String) updateData.get("topic"));
            }

            if (updateData.containsKey("start_time")) {
                String startTimeStr = (String) updateData.get("start_time");
                meeting.setStartTime(LocalDateTime.parse(startTimeStr));
            }

            if (updateData.containsKey("duration")) {
                Integer durationMinutes = (Integer) updateData.get("duration");
                meeting.setEndTime(meeting.getStartTime().plusMinutes(durationMinutes));
            }

            return meetingRepository.save(meeting);
        } catch (Exception e) {
            logger.error("Błąd podczas aktualizacji spotkania: {}", e.getMessage());
            throw new RuntimeException("Nie można zaktualizować spotkania", e);
        }
    }

    @Override
    @Transactional
    public Meeting createZoomMeeting(String title, LocalDateTime startTime, int durationMinutes, UUID hostUserId) {
        try {
            Optional<Users> hostOpt = userRepository.findById(hostUserId);
            if (!hostOpt.isPresent()) {
                throw new IllegalArgumentException("Nie znaleziono użytkownika o id: " + hostUserId);
            }

            Users host = hostOpt.get();

            if (host.getZoomUserId() == null || host.getZoomAccessToken() == null) {
                throw new IllegalArgumentException("Użytkownik nie jest połączony z Zoom");
            }

            // Pobierz klienta platformy
            MeetingPlatformClient platformClient = platformFactory.getClient("ZOOM");

            // Przygotuj dodatkowe opcje
            Map<String, Object> options = new HashMap<>();
            options.put("hostVideo", true);
            options.put("participantVideo", true);
            options.put("joinBeforeHost", false);
            options.put("muteUponEntry", true);
            options.put("autoRecording", "cloud");
            options.put("waitingRoom", true);

            // Utworzenie spotkania
            JsonNode response = platformClient.createMeeting(
                    title, startTime, durationMinutes, host.getZoomUserId(), options);

            // Utworzenie encji
            Meeting meeting = new Meeting();
            meeting.setTitle(title);
            meeting.setStartTime(startTime);
            meeting.setEndTime(startTime.plusMinutes(durationMinutes));
            meeting.setStatus(Status.PLANNED);
            meeting.setPlatform(Platform.ZOOM);
            meeting.setZoomHostId(host.getZoomUserId());
            meeting.setZoomJoinUrl(response.get("join_url").asText());
            meeting.setZoomMeetingId(response.get("id").asText());

            return meetingRepository.save(meeting);
        } catch (Exception e) {
            logger.error("Error creating zoom meeting", e);
            throw new RuntimeException("Błąd tworzenia spotkania");
        }
    }

    /**
     * METODY POMOCNICZE
     */
    private Optional<Meeting> findMeetingByZoomId(String zoomMeetingId) {
        return meetingRepository.findAll().stream()
                .filter(m -> zoomMeetingId.equals(m.getZoomMeetingId()))
                .findFirst();
    }
}