package com.licencjat.BusinessAssistant.webhook.zoom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import com.licencjat.BusinessAssistant.model.zoom.webhook.ZoomWebhookEvent;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.webhook.WebhookHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
public class ZoomMeetingStartedHandler implements WebhookHandler {
    private static final Logger logger = LoggerFactory.getLogger(ZoomMeetingStartedHandler.class);
    private final ObjectMapper objectMapper;
    private final MeetingRepository meetingRepository;

    @Autowired
    public ZoomMeetingStartedHandler(ObjectMapper objectMapper, MeetingRepository meetingRepository) {
        this.objectMapper = objectMapper;
        this.meetingRepository = meetingRepository;
    }

    @Override
    public boolean canHandle(String eventType) {
        return "meeting.started".equals(eventType);
    }

    @Override
    @Transactional
    public void handle(Map<String, Object> webhookData) {
        try {
            ZoomWebhookEvent event = objectMapper.convertValue(webhookData, ZoomWebhookEvent.class);
            String meetingId = event.getPayload().getObject().getId();

            logger.info("Meeting started: {}", meetingId);

            Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

            if (meetingOptional.isPresent()) {
                Meeting meeting = meetingOptional.get();
                meeting.setStatus(Status.ONGOING);
                meeting.setStartTime(LocalDateTime.now());
                meetingRepository.save(meeting);
                logger.info("Zaktualizowano status spotkania na trwające");
            } else {
                logger.warn("Nie znaleziono spotkania o id: {}", meetingId);
            }
        } catch (Exception e) {
            logger.error("Błąd podczas obsługi webhooka meeting.started", e);
            throw new RuntimeException("Błąd przetwarzania webhooka", e);
        }
    }

    private Optional<Meeting> findMeetingByZoomId(String zoomMeetingId) {
        return meetingRepository.findAll().stream()
                .filter(m -> zoomMeetingId.equals(m.getZoomMeetingId()))
                .findFirst();
    }
}