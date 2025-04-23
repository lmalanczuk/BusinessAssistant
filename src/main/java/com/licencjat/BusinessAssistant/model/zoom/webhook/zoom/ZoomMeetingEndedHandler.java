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
public class ZoomMeetingEndedHandler implements WebhookHandler {
    private static final Logger logger = LoggerFactory.getLogger(ZoomMeetingEndedHandler.class);
    private final ObjectMapper objectMapper;
    private final MeetingRepository meetingRepository;

    @Autowired
    public ZoomMeetingEndedHandler(ObjectMapper objectMapper, MeetingRepository meetingRepository) {
        this.objectMapper = objectMapper;
        this.meetingRepository = meetingRepository;
    }

    @Override
    public boolean canHandle(String eventType) {
        return "meeting.ended".equals(eventType);
    }

    @Override
    @Transactional
    public void handle(Map<String, Object> webhookData) {
        try {
            ZoomWebhookEvent event = objectMapper.convertValue(webhookData, ZoomWebhookEvent.class);
            String meetingId = event.getPayload().getObject().getId();

            logger.info("Meeting ended: {}", meetingId);

            Optional<Meeting> meetingOptional = findMeetingByZoomId(meetingId);

            if (meetingOptional.isPresent()) {
                Meeting meeting = meetingOptional.get();
                meeting.setStatus(Status.COMPLETED);
                meeting.setEndTime(LocalDateTime.now());
                meetingRepository.save(meeting);
                logger.info("Zaktualizowano status spotkania na zakończone");
            } else {
                logger.warn("Nie znaleziono spotkania o id: {}", meetingId);
            }
        } catch (Exception e) {
            logger.error("Błąd podczas obsługi webhooka meeting.ended", e);
            throw new RuntimeException("Błąd przetwarzania webhooka", e);
        }
    }

    private Optional<Meeting> findMeetingByZoomId(String zoomMeetingId) {
        return meetingRepository.findAll().stream()
                .filter(m -> zoomMeetingId.equals(m.getZoomMeetingId()))
                .findFirst();
    }
}