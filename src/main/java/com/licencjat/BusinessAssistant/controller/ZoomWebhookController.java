package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.model.response.ApiResponse;
import com.licencjat.BusinessAssistant.security.ZoomWebhookValidator;
import com.licencjat.BusinessAssistant.service.ZoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/zoom/webhook")
public class ZoomWebhookController {
    private static final Logger logger = LoggerFactory.getLogger(ZoomWebhookController.class);

    private final ZoomService zoomService;
    private final ZoomWebhookValidator webhookValidator;

    public ZoomWebhookController(ZoomService zoomService, ZoomWebhookValidator webhookValidator) {
        this.zoomService = zoomService;
        this.webhookValidator = webhookValidator;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> handleWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Zoom-Signature", required = false) String signature,
            @RequestHeader(value = "X-Zoom-Request-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> webhookData) {

        try {
            logger.info("Received webhook from Zoom");

            // First try v2 signature validation
            if (signature != null && timestamp != null) {
                if (!webhookValidator.isValidSignature(rawPayload, timestamp, signature)) {
                    logger.warn("Invalid webhook signature");
                    return ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(new ApiResponse(false, "Invalid webhook signature"));
                }
            }
            // Fall back to token validation
            else if (token != null) {
                if (!webhookValidator.isValidToken(token)) {
                    logger.warn("Invalid webhook token");
                    return ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body(new ApiResponse(false, "Invalid webhook token"));
                }
            }
            // No validation info provided
            else {
                logger.warn("No validation info provided with webhook");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "No validation info provided"));
            }

            String eventType = (String) webhookData.get("event");
            logger.info("Received webhook with event type: {}", eventType);

            // Process the webhook
            zoomService.processWebhook(webhookData);

            return ResponseEntity.ok(new ApiResponse(true, "Webhook processed successfully"));
        } catch (Exception e) {
            logger.error("Error processing Zoom webhook", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error processing webhook: " + e.getMessage()));
        }
    }
}