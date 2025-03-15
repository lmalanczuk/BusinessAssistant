package com.licencjat.BusinessAssistant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.licencjat.BusinessAssistant.client.ZoomClient;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.model.MeetingDTO;
import com.licencjat.BusinessAssistant.model.request.CreateZoomMeetingRequest;
import com.licencjat.BusinessAssistant.model.response.ApiResponse;
import com.licencjat.BusinessAssistant.service.ZoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/zoom")
public class ZoomController {
    private static final Logger logger = LoggerFactory.getLogger(ZoomController.class);

    private final ZoomClient zoomClient;
    private final ZoomService zoomService;

    @Value("${zoom.redirect-uri}")
    private String redirectUri;

    @Value("${zoom.client-id}")
    private String clientId;

    public ZoomController(ZoomClient zoomClient, ZoomService zoomService) {
        this.zoomClient = zoomClient;
        this.zoomService = zoomService;
    }

    /**
     * Rozpoczyna proces autoryzacji OAuth z Zoom
     */
    @GetMapping("/auth")
    public RedirectView authorizeZoom() {
        String authUrl = "https://zoom.us/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri;
        return new RedirectView(authUrl);
    }

    /**
     * Obsługuje callback po autoryzacji OAuth
     */
    @GetMapping("/oauth/callback")
    public ResponseEntity<ApiResponse> oauthCallback(@RequestParam String code) {
        try {
            zoomClient.exchangeCodeForTokens(code, redirectUri);
            return ResponseEntity.ok(new ApiResponse(true, "Autoryzacja z Zoom zakończona sukcesem!"));
        } catch (Exception e) {
            logger.error("Błąd podczas autoryzacji z Zoom", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Błąd podczas autoryzacji z Zoom: " + e.getMessage()));
        }
    }

    /**
     * Obsługuje webhooki z Zoom
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse> handleWebhook(@RequestBody Map<String, Object> webhookData) {
        try {
            String eventType = (String) webhookData.get("event");
            logger.info("Otrzymano webhook z Zoom: {}", eventType);

            zoomService.processWebhook(webhookData);

            return ResponseEntity.ok(new ApiResponse(true, "Webhook przetworzony pomyślnie"));
        } catch (Exception e) {
            logger.error("Błąd podczas przetwarzania webhooka Zoom", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Błąd podczas przetwarzania webhooka: " + e.getMessage()));
        }
    }

    /**
     * Tworzy nowe spotkanie Zoom
     */
    @PostMapping("/meetings")
    public ResponseEntity<?> createMeeting(@RequestBody CreateZoomMeetingRequest request) {
        try {
            Meeting meeting = zoomService.createZoomMeeting(
                    request.getTitle(),
                    request.getStartTime(),
                    request.getDurationMinutes(),
                    request.getHostUserId()
            );

            MeetingDTO meetingDTO = convertToDTO(meeting);
            return ResponseEntity.status(HttpStatus.CREATED).body(meetingDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Błąd podczas tworzenia spotkania", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Nie można utworzyć spotkania: " + e.getMessage()));
        }
    }

    /**
     * Pobiera szczegóły spotkania z Zoom
     */
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<?> getMeetingDetails(@PathVariable String meetingId) {
        try {
            JsonNode meetingDetails = zoomClient.getMeeting(meetingId);
            return ResponseEntity.ok(meetingDetails);
        } catch (Exception e) {
            logger.error("Błąd podczas pobierania szczegółów spotkania {}", meetingId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Nie można pobrać szczegółów spotkania: " + e.getMessage()));
        }
    }

    /**
     * Pobiera nagrania dla spotkania
     */
    @GetMapping("/meetings/{meetingId}/recordings")
    public ResponseEntity<?> getMeetingRecordings(@PathVariable String meetingId) {
        try {
            JsonNode recordings = zoomClient.getMeetingRecordings(meetingId);
            return ResponseEntity.ok(recordings);
        } catch (Exception e) {
            logger.error("Błąd podczas pobierania nagrań dla spotkania {}", meetingId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Nie można pobrać nagrań: " + e.getMessage()));
        }
    }

    /**
     * Pobiera nagrania użytkownika z określonego okresu
     */
    @GetMapping("/users/{userId}/recordings")
    public ResponseEntity<?> getUserRecordings(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String to) {
        try {
            JsonNode recordings = zoomClient.getUserRecordings(userId, from, to);
            return ResponseEntity.ok(recordings);
        } catch (Exception e) {
            logger.error("Błąd podczas pobierania nagrań dla użytkownika {}", userId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Nie można pobrać nagrań: " + e.getMessage()));
        }
    }

    /**
     * Konwertuje obiekt Meeting na MeetingDTO
     */
    private MeetingDTO convertToDTO(Meeting meeting) {
        MeetingDTO dto = new MeetingDTO();
        dto.setId(meeting.getId());
        dto.setTitle(meeting.getTitle());
        dto.setStartTime(meeting.getStartTime());
        dto.setEndTime(meeting.getEndTime());
        dto.setStatus(meeting.getStatus());
        dto.setPlatform(meeting.getPlatform());
        return dto;
    }
}