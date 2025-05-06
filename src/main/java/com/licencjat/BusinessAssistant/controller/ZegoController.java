package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.exception.ResourceNotFoundException;
import com.licencjat.BusinessAssistant.model.MeetingDTO;
import com.licencjat.BusinessAssistant.model.request.CreateMeetingRequest;
import com.licencjat.BusinessAssistant.model.response.ApiResponse;
import com.licencjat.BusinessAssistant.model.response.TokenResponse;
import com.licencjat.BusinessAssistant.security.UserPrincipal;
import com.licencjat.BusinessAssistant.service.ZegoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/zego")
public class ZegoController {
    private static final Logger logger = LoggerFactory.getLogger(ZegoController.class);

    private final ZegoService zegoService;

    @Autowired
    public ZegoController(ZegoService zegoService) {
        this.zegoService = zegoService;
    }

    /**
     * Generuje token dla użytkownika
     */
    @GetMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TokenResponse> generateToken(
            @RequestParam String roomId,
            @RequestParam(required = false, defaultValue = "3600") int expireTime) {
        try {
            // Pobierz aktualnego użytkownika
            UserPrincipal userPrincipal = getCurrentUser();
            if (userPrincipal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenResponse(null, "Użytkownik nie jest uwierzytelniony"));
            }

            // Generuj token
            String token = zegoService.generateToken(userPrincipal.getId().toString(), roomId, expireTime);

            return ResponseEntity.ok(new TokenResponse(token, "Token wygenerowany pomyślnie"));
        } catch (Exception e) {
            logger.error("Błąd generowania tokenu: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TokenResponse(null, "Błąd generowania tokenu: " + e.getMessage()));
        }
    }

    /**
     * Tworzy nowe spotkanie
     */
    @PostMapping("/meetings")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<?> createMeeting(@Valid @RequestBody CreateMeetingRequest request) {
    try {
        logger.info("Otrzymano żądanie utworzenia spotkania: {}", request);

        UserPrincipal userPrincipal = getCurrentUser();
        if (userPrincipal == null) {
            logger.warn("Brak uwierzytelnionego użytkownika");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Użytkownik nie jest uwierzytelniony"));
        }

        logger.info("Tworzenie spotkania dla użytkownika: {}", userPrincipal.getId());

        Meeting meeting = zegoService.createMeeting(
                request.getTitle(),
                request.getStartTime(),
                request.getDurationMinutes(),
                userPrincipal.getId()
        );

        MeetingDTO meetingDTO = convertToDTO(meeting);
        logger.info("Spotkanie utworzone pomyślnie: {}", meeting.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(meetingDTO);
    } catch (ResourceNotFoundException e) {
        logger.error("Nie znaleziono zasobu: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, e.getMessage()));
    } catch (Exception e) {
        logger.error("Błąd podczas tworzenia spotkania: {}",
                e.getMessage() != null ? e.getMessage() : "Nieznany błąd", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Nie można utworzyć spotkania: " +
                        (e.getMessage() != null ? e.getMessage() : "Wystąpił nieznany błąd")));
    }
}

    /**
     * Aktualizuje status spotkania (start/end)
     */
    @PutMapping("/meetings/{meetingId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMeetingStatus(
            @PathVariable UUID meetingId,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String status = statusUpdate.get("status");
            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Status nie może być pusty"));
            }

            Meeting meeting;

            if ("start".equals(status)) {
                meeting = zegoService.startMeeting(meetingId);
            } else if ("end".equals(status)) {
                meeting = zegoService.endMeeting(meetingId);
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Nieprawidłowy status. Dostępne opcje: 'start', 'end'"));
            }

            return ResponseEntity.ok(convertToDTO(meeting));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Błąd podczas aktualizacji statusu spotkania: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Błąd podczas aktualizacji statusu spotkania: " + e.getMessage()));
        }
    }

    /**
     * Pobiera aktualnego użytkownika z kontekstu bezpieczeństwa
     */
    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        return (UserPrincipal) authentication.getPrincipal();
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