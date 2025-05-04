package com.licencjat.BusinessAssistant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.licencjat.BusinessAssistant.client.zoom.ZoomAuthClient;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.exception.AuthenticationException;
import com.licencjat.BusinessAssistant.exception.ResourceNotFoundException;
import com.licencjat.BusinessAssistant.model.MeetingDTO;
import com.licencjat.BusinessAssistant.model.request.CreateZoomMeetingRequest;
import com.licencjat.BusinessAssistant.model.response.ApiResponse;
import com.licencjat.BusinessAssistant.model.zoom.ZoomAuthResponse;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.security.UserPrincipal;
import com.licencjat.BusinessAssistant.service.ZoomService;
import com.licencjat.BusinessAssistant.util.JwtTokenProvider;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    // ZMIANA: Zaczynamy tylko z podstawowym scope user:read
    private static final String ZOOM_SCOPES = "user:read";

    private final ZoomAuthClient zoomClient;
    private final ZoomService zoomService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${zoom.redirect-uri}")
    private String redirectUri;

    @Value("${zoom.client-id}")
    private String clientId;

    public ZoomController(ZoomAuthClient zoomClient, ZoomService zoomService, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
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
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(ZOOM_SCOPES, StandardCharsets.UTF_8);

        logger.info("Redirecting to Zoom authorization URL with minimal scopes: {}", ZOOM_SCOPES);
        logger.debug("Full authorization URL: {}", authUrl);
        return new RedirectView(authUrl);
    }

    /**
     * Obsługuje callback po autoryzacji OAuth
     */
    @GetMapping("/oauth/callback")
    public ResponseEntity<ApiResponse> oauthCallback(@RequestParam String code, @RequestParam(required = false) String state) {
        try {
            logger.info("Received OAuth callback with code: {} and state: {}",
                code != null ? code.substring(0, 10) + "..." : "null",
                state != null ? state.substring(0, 10) + "..." : "null");

            logger.debug("Configured redirect URI: {}", redirectUri);

            // Jeśli state jest dostępne, zweryfikuj token
            UUID userId = null;
            if (state != null && !state.isEmpty()) {
                userId = jwtTokenProvider.getUserIdFromZoomStateToken(state);
                if (userId == null) {
                    logger.error("Failed to extract user ID from state token");
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "Nieprawidłowy token state"));
                }
                logger.info("Extracted user ID from state: {}", userId);
            }

            // Pobierz tokeny z Zoom
            ZoomAuthResponse authResponse = zoomClient.exchangeCodeForTokens(code, redirectUri);
            logger.info("Token exchange successful, access token received: {}",
                authResponse.getAccessToken() != null ? "yes" : "no");

            // Jeśli mamy userId, oznacza to, że użytkownik łączy swoje konto
            if (userId != null) {
                // Krótka przerwa, aby upewnić się, że token został ustawiony
                Thread.sleep(100);

                // Token został już ustawiony w ZoomTokenManager podczas exchangeCodeForTokens
                // Pobierz informacje o użytkowniku Zoom
                JsonNode userInfo = zoomClient.getUserInfo();
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
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "Nie znaleziono użytkownika"));
                }
            } else {
                // Przypadek 1 - prosta autoryzacja bez łączenia konta
                return ResponseEntity.ok(new ApiResponse(true, "Autoryzacja z Zoom zakończona sukcesem!"));
            }
        } catch (AuthenticationException e) {
            logger.error("Authentication error during Zoom authorization: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Błąd autoryzacji z Zoom: " + e.getMessage()));
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found during Zoom authorization: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during Zoom authorization", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Błąd podczas autoryzacji z Zoom: " + e.getMessage()));
        }
    }


    /**
     * Tworzy nowe spotkanie Zoom
     */
    @PostMapping("/meetings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createMeeting(@Valid @RequestBody CreateZoomMeetingRequest request) {
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
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMeetingDetails(@PathVariable String meetingId) {
        try {
            JsonNode meetingDetails = zoomService.getMeeting(meetingId);
            return ResponseEntity.ok(meetingDetails);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMeetingRecordings(@PathVariable String meetingId) {
        try {
            JsonNode recordings = zoomService.getMeetingRecordings(meetingId);
            return ResponseEntity.ok(recordings);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserRecordings(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String to) {
        try {
            JsonNode recordings = zoomService.getUserRecordings(userId, from, to);
            return ResponseEntity.ok(recordings);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Błąd podczas pobierania nagrań dla użytkownika {}", userId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Nie można pobrać nagrań: " + e.getMessage()));
        }
    }

    /**
     * Łączy konto użytkownika z kontem Zoom
     */
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

            // Dodajemy minimalne wymagane scopes do URL autoryzacji
            String authUrl = "https://zoom.us/oauth/authorize" +
                    "?response_type=code" +
                    "&client_id=" + clientId +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                    "&state=" + state +
                    "&scope=" + URLEncoder.encode(ZOOM_SCOPES, StandardCharsets.UTF_8);

            logger.info("URL autoryzacji Zoom z minimalnym scope: {}", authUrl);
            return new RedirectView(authUrl);
        } catch (Exception e) {
            logger.error("Błąd podczas łączenia z Zoom: {}", e.getMessage(), e);
            return new RedirectView("/error?message=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Aktualizuje status spotkania (start/end)
     */
    @PutMapping("/meetings/{meetingId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMeetingStatus(
            @PathVariable String meetingId,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String status = statusUpdate.get("status");
            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Status nie może być pusty"));
            }

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
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMeetingParticipants(@PathVariable String meetingId) {
        try {
            JsonNode participants = zoomService.getMeetingParticipants(meetingId);
            return ResponseEntity.ok(participants);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
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
    @PreAuthorize("isAuthenticated()")
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
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMeeting(
            @PathVariable String meetingId,
            @RequestBody Map<String, Object> updateData) {
        try {
            if (updateData == null || updateData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Dane aktualizacji nie mogą być puste"));
            }

            Meeting meeting = zoomService.updateMeeting(meetingId, updateData);
            MeetingDTO meetingDTO = convertToDTO(meeting);
            return ResponseEntity.ok(meetingDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
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
     * Testowy endpoint dla sprawdzenia autoryzacji
     */
    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Map<String, Object> response = new HashMap<>();

            response.put("authenticated", auth != null && auth.isAuthenticated());

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
     * Testowy endpoint dla konfiguracji Zoom
     */
    @GetMapping("/test-zoom-config")
    public ResponseEntity<?> testZoomConfig() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("clientId", clientId);
            response.put("redirectUri", redirectUri);
            response.put("scopes", ZOOM_SCOPES);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
                UUID userId = userPrincipal.getId();
                response.put("userId", userId);

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
     * Debugowy endpoint do łączenia kont Zoom
     */
    @GetMapping("/connect-debug")
    public ResponseEntity<?> connectZoomAccountDebug(@RequestParam(required = false) String userId) {
        try {
            UUID userUUID = null;
            if (userId != null && !userId.isEmpty()) {
                userUUID = UUID.fromString(userId);
            } else {
                List<Users> users = userRepository.findAll();
                if (!users.isEmpty()) {
                    userUUID = users.get(0).getId();
                } else {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false, "Brak użytkowników w bazie danych"));
                }
            }

            String state = jwtTokenProvider.generateZoomStateToken(userUUID);

            // Upewnijmy się, że redirect_uri jest poprawnie zakodowany
            String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userUUID);
            response.put("state", state);
            response.put("clientId", clientId);
            response.put("redirectUri", redirectUri);
            response.put("encodedRedirectUri", encodedRedirectUri);
            response.put("scopes", ZOOM_SCOPES);

            String authUrl = "https://zoom.us/oauth/authorize" +
                    "?response_type=code" +
                    "&client_id=" + clientId +
                    "&redirect_uri=" + encodedRedirectUri +
                    "&state=" + state +
                    "&scope=" + URLEncoder.encode(ZOOM_SCOPES, StandardCharsets.UTF_8);

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
     * Endpoint diagnostyczny dla konfiguracji Zoom App
     */
    @GetMapping("/validate-config")
    public ResponseEntity<?> validateZoomConfig() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("clientId", clientId);
            response.put("redirectUri", redirectUri);
            response.put("encodedRedirectUri", URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
            response.put("scopes", ZOOM_SCOPES);

            // Sprawdź czy redirect uri nie zawiera znaków specjalnych
            boolean hasSpecialChars = !redirectUri.matches("^[a-zA-Z0-9:/._-]+$");
            response.put("hasSpecialCharsInUri", hasSpecialChars);

            logger.info("Zoom config validation: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validating Zoom config", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Konwertuje obiekt Meeting na MeetingDTO
     */
    private MeetingDTO convertToDTO(Meeting meeting) {
        if (meeting == null) {
            return null;
        }

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