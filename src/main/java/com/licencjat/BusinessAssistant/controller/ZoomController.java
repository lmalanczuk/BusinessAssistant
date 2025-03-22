package com.licencjat.BusinessAssistant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.model.zoom.ZoomAuthResponse;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.security.UserPrincipal;
import com.licencjat.BusinessAssistant.util.JwtTokenProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/zoom")
public class ZoomController {
    private static final Logger logger = LoggerFactory.getLogger(ZoomController.class);

    private final ZoomClient zoomClient;
    private final ZoomService zoomService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${zoom.redirect-uri}")
    private String redirectUri;

    @Value("${zoom.client-id}")
    private String clientId;

    public ZoomController(ZoomClient zoomClient, ZoomService zoomService, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.zoomClient = zoomClient;
        this.zoomService = zoomService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
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

    @GetMapping("/connect")
    @PreAuthorize("isAuthenticated()")
    public RedirectView connectZoomAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getId();

        String state = jwtTokenProvider.generateZoomStateToken(userId);

        String authUrl = "https://zoom.us/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;

        return new RedirectView(authUrl);
    }

 @GetMapping("/oauth/callback")
    public ResponseEntity<ApiResponse> oauthCallback(@RequestParam String code, @RequestParam String state) {
        try {
            // Zweryfikuj state token i pobierz ID użytkownika
            UUID userId = jwtTokenProvider.getUserIdFromZoomStateToken(state);
            if (userId == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Nieprawidłowy token state"));
            }

            // Pobierz tokeny z Zoom
            ZoomAuthResponse authResponse = zoomClient.exchangeCodeForTokens(code, redirectUri);

            // Pobierz informacje o użytkowniku Zoom
            JsonNode userInfo = zoomClient.getUserInfo(authResponse.getAccessToken());
            String zoomUserId = userInfo.get("id").asText();

            // Zaktualizuj użytkownika w bazie
            Optional<Users> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                Users user = userOpt.get();
                user.setZoomUserId(zoomUserId);
                user.setZoomAccessToken(authResponse.getAccessToken());
                user.setZoomRefreshToken(authResponse.getRefreshToken());
                user.setZoomTokenExpiry(LocalDateTime.now().plusSeconds(authResponse.getExpiresIn()));
                userRepository.save(user);

                return ResponseEntity.ok(new ApiResponse(true, "Konto Zoom zostało pomyślnie połączone"));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Nie znaleziono użytkownika"));
            }
        } catch (Exception e) {
            logger.error("Błąd podczas łączenia konta Zoom", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Błąd podczas łączenia konta Zoom: " + e.getMessage()));
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