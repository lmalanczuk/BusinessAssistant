package com.licencjat.BusinessAssistant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.exception.AuthenticationException;
import com.licencjat.BusinessAssistant.exception.ResourceNotFoundException;
import com.licencjat.BusinessAssistant.model.zoom.ZoomAuthResponse;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.security.UserPrincipal;
import com.licencjat.BusinessAssistant.util.JwtTokenProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
     * Uwaga: Usunięto zduplikowaną metodę, pozostawiono tylko tę wersję z obsługą parametru state
     */
    @GetMapping("/oauth/callback")
    public ResponseEntity<ApiResponse> oauthCallback(@RequestParam String code, @RequestParam(required = false) String state) {
        try {
            // Jeśli state jest dostępne, zweryfikuj token
            UUID userId = null;
            if (state != null && !state.isEmpty()) {
                userId = jwtTokenProvider.getUserIdFromZoomStateToken(state);
                if (userId == null) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "Nieprawidłowy token state"));
                }
            }

            // Pobierz tokeny z Zoom
            ZoomAuthResponse authResponse = zoomClient.exchangeCodeForTokens(code, redirectUri);

            // Jeśli mamy userId, oznacza to, że użytkownik łączy swoje konto
            if (userId != null) {
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
            } else {
                // Przypadek 1 - prosta autoryzacja bez łączenia konta
                return ResponseEntity.ok(new ApiResponse(true, "Autoryzacja z Zoom zakończona sukcesem!"));
            }
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
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Authentication: {}, Principal type: {}",
                   authentication,
                   authentication.getPrincipal().getClass().getName());

        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            logger.error("Nieprawidłowy typ principal: {}", authentication.getPrincipal().getClass().getName());
            throw new AuthenticationException("Nieprawidłowy typ principal");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getId();
        logger.info("Próba połączenia konta Zoom dla użytkownika: {}", userId);

        Optional<Users> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            logger.error("Nie znaleziono użytkownika o ID: {}", userId);
            throw new ResourceNotFoundException("Nie znaleziono użytkownika");
        }

        String state = jwtTokenProvider.generateZoomStateToken(userId);
        logger.info("Wygenerowano token state: {}", state);

        String authUrl = "https://zoom.us/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;

        logger.info("URL autoryzacji Zoom: {}", authUrl);
        return new RedirectView(authUrl);
    } catch (Exception e) {
        logger.error("Błąd podczas łączenia z Zoom: {}", e.getMessage(), e);
        // Ponieważ to RedirectView, nie możemy zwrócić odpowiedzi z błędem
        // Przekierujmy do specjalnej strony błędu lub URL
        return new RedirectView("/error?message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
    }
}
    /**
 * Aktualizuje status spotkania (start/end)
 */
@PutMapping("/meetings/{meetingId}/status")
public ResponseEntity<?> updateMeetingStatus(
        @PathVariable String meetingId,
        @RequestBody Map<String, String> statusUpdate) {
    try {
        String status = statusUpdate.get("status");
        if ("start".equals(status)) {
            Meeting meeting = zoomService.startMeeting(meetingId);
            MeetingDTO meetingDTO = convertToDTO(meeting);
            return ResponseEntity.ok(meetingDTO);
        } else if ("end".equals(status)) {
            Meeting meeting = zoomService.endMeeting(meetingId);
            MeetingDTO meetingDTO = convertToDTO(meeting);
            return ResponseEntity.ok(meetingDTO);
        } else {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Nieprawidłowy status. Dostępne opcje: 'start', 'end'"));
        }
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
    } catch (Exception e) {
        logger.error("Błąd podczas aktualizacji statusu spotkania {}", meetingId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Błąd podczas aktualizacji statusu spotkania: " + e.getMessage()));
    }
}

/**
 * Pobiera listę uczestników spotkania
 */
@GetMapping("/meetings/{meetingId}/participants")
public ResponseEntity<?> getMeetingParticipants(@PathVariable String meetingId) {
    try {
        JsonNode participants = zoomService.getMeetingParticipants(meetingId);
        return ResponseEntity.ok(participants);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
    } catch (Exception e) {
        logger.error("Błąd podczas pobierania uczestników spotkania {}", meetingId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Błąd podczas pobierania uczestników: " + e.getMessage()));
    }
}

/**
 * Wysyła zaproszenia do spotkania
 */
@PostMapping("/meetings/{meetingId}/invite")
public ResponseEntity<?> inviteToMeeting(
        @PathVariable String meetingId,
        @RequestBody Map<String, List<String>> inviteRequest) {
    try {
        List<String> emails = inviteRequest.get("emails");
        if (emails == null || emails.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lista adresów email jest pusta"));
        }

        JsonNode result = zoomService.inviteToMeeting(meetingId, emails);
        return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
    } catch (Exception e) {
        logger.error("Błąd podczas wysyłania zaproszeń do spotkania {}", meetingId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Błąd podczas wysyłania zaproszeń: " + e.getMessage()));
    }
}

/**
 * Aktualizuje dane spotkania
 */
@PatchMapping("/meetings/{meetingId}")
public ResponseEntity<?> updateMeeting(
        @PathVariable String meetingId,
        @RequestBody Map<String, Object> updateData) {
    try {
        Meeting meeting = zoomService.updateMeeting(meetingId, updateData);
        MeetingDTO meetingDTO = convertToDTO(meeting);
        return ResponseEntity.ok(meetingDTO);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage()));
    } catch (Exception e) {
        logger.error("Błąd podczas aktualizacji spotkania {}", meetingId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Błąd podczas aktualizacji spotkania: " + e.getMessage()));
    }
}

/**
 * Endpointy testowe
 */
/**
 * Prosty endpoint testowy do sprawdzenia autoryzacji
 */
@GetMapping("/test-auth")
public ResponseEntity<?> testAuth() {
    try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();

        response.put("authenticated", auth != null && auth.isAuthenticated());

        // Sprawdź, czy to jest UserPrincipal
        if (auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            response.put("userId", userPrincipal.getId());
            response.put("username", userPrincipal.getUsername());
            response.put("email", userPrincipal.getEmail());
        } else {
            response.put("principal", auth.getPrincipal().toString());
        }

        response.put("authorities", auth.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .collect(Collectors.toList()));

        logger.info("Test autoryzacji udany: {}", response);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        logger.error("Błąd podczas testu autoryzacji", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Błąd: " + e.getMessage()));
    }
}

/**
 * Prosty endpoint testowy dla Zoom bez przekierowania
 */
@GetMapping("/test-zoom-config")
public ResponseEntity<?> testZoomConfig() {
    try {
        Map<String, Object> response = new HashMap<>();
        response.put("clientId", clientId);
        response.put("redirectUri", redirectUri);

        // Sprawdź czy użytkownik jest zalogowany
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            UUID userId = userPrincipal.getId();
            response.put("userId", userId);

            // Sprawdź dane użytkownika w bazie
            Optional<Users> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                Users user = userOpt.get();
                response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "hasZoomId", user.getZoomUserId() != null
                ));
            }
        }

        logger.info("Test konfiguracji Zoom: {}", response);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        logger.error("Błąd podczas testu konfiguracji Zoom", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Błąd: " + e.getMessage()));
    }
}

/**
 * Wersja endpointu connect bez autoryzacji - do celów testowych
 */
@GetMapping("/connect-debug")
public ResponseEntity<?> connectZoomAccountDebug(@RequestParam(required = false) String userId) {
    try {
        UUID userUUID = null;
        if (userId != null && !userId.isEmpty()) {
            userUUID = UUID.fromString(userId);
        } else {
            // Pobierz dowolnego użytkownika z bazy jako przykład
            List<Users> users = userRepository.findAll();
            if (!users.isEmpty()) {
                userUUID = users.get(0).getId();
            } else {
                return ResponseEntity.badRequest()
                       .body(new ApiResponse(false, "Brak użytkowników w bazie danych"));
            }
        }

        String state = jwtTokenProvider.generateZoomStateToken(userUUID);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userUUID);
        response.put("state", state);
        response.put("clientId", clientId);
        response.put("redirectUri", redirectUri);

        String authUrl = "https://zoom.us/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;

        response.put("authUrl", authUrl);

        logger.info("Debug połączenia z Zoom: {}", response);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        logger.error("Błąd debug połączenia z Zoom", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Błąd: " + e.getMessage()));
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