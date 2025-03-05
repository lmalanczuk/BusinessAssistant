package com.licencjat.BusinessAssistant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.config.ZoomConfig;
import com.licencjat.BusinessAssistant.service.ZoomWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zoom/webhook")
public class ZoomWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(ZoomWebhookController.class);
    private final ZoomConfig zoomConfig;
    private final ZoomWebhookService zoomWebhookService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ZoomWebhookController(ZoomConfig zoomConfig, ZoomWebhookService zoomWebhookService) {
        this.zoomConfig = zoomConfig;
        this.zoomWebhookService = zoomWebhookService;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("authorization") String authorization,
            @RequestBody String payload) {

        if (!authorization.equals(zoomConfig.getVerificationToken())) {
            logger.warn("Invalid verification token received");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            JsonNode webhookData = objectMapper.readTree(payload);
            String eventType = webhookData.path("event").asText();

            switch (eventType) {
                case "meeting.started":
                    zoomWebhookService.handleMeetingStarted(webhookData);
                    break;
                case "meeting.ended":
                    zoomWebhookService.handleMeetingEnded(webhookData);
                    break;
                case "recording.completed":
                    zoomWebhookService.handleRecordingCompleted(webhookData);
                    break;
                default:
                    logger.info("Received unhandled Zoom webhook event: {}", eventType);
            }

            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            logger.error("Error processing Zoom webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook");
        }
    }
}