//package com.licencjat.BusinessAssistant.model.zoom.webhook;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.licencjat.BusinessAssistant.entity.Meeting;
//import com.licencjat.BusinessAssistant.entity.enums.Status;
//import com.licencjat.BusinessAssistant.repository.MeetingRepository;
//import com.licencjat.BusinessAssistant.webhook.WebhookHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.Optional;
//
//@Component
//public class ZoomMeetingStartedHandler implements WebhookHandler {
//    private static final Logger logger = LoggerFactory.getLogger(ZoomMeetingStartedHandler.class);
//    private final ObjectMapper objectMapper;
//    private final MeetingRepository meetingRepository;
//
//    @Autowired
//    public ZoomMeetingStartedHandler(ObjectMapper objectMapper, MeetingRepository meetingRepository) {
//        this.objectMapper = objectMapper;
//        this.meetingRepository = meetingRepository;
//    }
//
//    @Override
//    public boolean canHandle(String eventType) {
//        return "meeting.started".equals(eventType);
//    }
//
//    @Override
//    @Transactional
//    public void handle(Map<String, Object> webhookData) {
//        try {
//            ZoomWebhookEvent event = objectMapper.convertValue(webhookData, ZoomWebhookEvent.class);
//            String meetingId = event.getPayload().getObject().getId();
//
//            logger.info("Meeting started: {}", meetingId);
//
//            Optional<Meeting> meetingOptional = meetingRepository.findByZoomMeetingId(meetingId);
//
//            if (meetingOptional.isPresent()) {
//                Meeting meeting = meetingOptional.get();
//                meeting.setStatus(Status.ONGOING);
//                meeting.setStartTime(LocalDateTime.now());
//                meetingRepository.save(meeting);
//                logger.info("Updated meeting status to ONGOING");
//            } else {
//                logger.warn("Meeting not found with ID: {}", meetingId);
//            }
//        } catch (Exception e) {
//            logger.error("Error handling meeting.started webhook", e);
//            throw new RuntimeException("Error processing webhook", e);
//        }
//    }
//}