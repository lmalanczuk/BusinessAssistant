package com.licencjat.BusinessAssistant.service;

import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.entity.enums.Platform;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import com.licencjat.BusinessAssistant.exception.ResourceNotFoundException;
import com.licencjat.BusinessAssistant.exception.ZegoApiException;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.util.ZegoTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ZegoService {
    private static final Logger logger = LoggerFactory.getLogger(ZegoService.class);

    private final ZegoTokenGenerator tokenGenerator;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Autowired
    public ZegoService(ZegoTokenGenerator tokenGenerator, MeetingRepository meetingRepository, UserRepository userRepository) {
        this.tokenGenerator = tokenGenerator;
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generuje token dla użytkownika
     *
     * @param userId ID użytkownika
     * @param roomId ID pokoju
     * @param effectiveTimeInSeconds Czas ważności tokenu w sekundach
     * @return Wygenerowany token
     */
    public String generateToken(String userId, String roomId, int effectiveTimeInSeconds) {
        try {
            // Standardowo dajemy uprawnienia do publikowania (2)
            return tokenGenerator.generateToken(userId, roomId, 2, effectiveTimeInSeconds);
        } catch (Exception e) {
            logger.error("Błąd generowania tokenu: {}", e.getMessage());
            throw new ZegoApiException("Nie można wygenerować tokenu", e);
        }
    }

    /**
     * Tworzy spotkanie
     *
     * @param title Tytuł spotkania
     * @param startTime Czas rozpoczęcia
     * @param durationMinutes Czas trwania w minutach
     * @param hostUserId ID użytkownika-hosta
     * @return Utworzone spotkanie
     */
    @Transactional
    public Meeting createMeeting(String title, LocalDateTime startTime, int durationMinutes, UUID hostUserId) {
    try {
        Users host = userRepository.findById(hostUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono użytkownika o ID: " + hostUserId));

        String roomId = UUID.randomUUID().toString();

        logger.info("Tworzenie spotkania: tytuł={}, rozpoczęcie={}, ID pokoju={}",
                title, startTime, roomId);

        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setStartTime(startTime);
        meeting.setEndTime(startTime.plusMinutes(durationMinutes));
        meeting.setStatus(Status.PLANNED);
        meeting.setPlatform(Platform.ZEGOCLOUD);
        meeting.setZegoRoomId(roomId);

        if (meeting.getParticipants() == null) {
            meeting.setParticipants(new HashSet<>());
        }

        meeting.addParticipant(host);

        Meeting savedMeeting = meetingRepository.save(meeting);
        logger.info("Utworzono spotkanie: {}", savedMeeting.getId());

        return savedMeeting;
    } catch (ResourceNotFoundException e) {
        throw e;
    } catch (Exception e) {
        logger.error("Błąd podczas tworzenia spotkania: {}",
                e.getMessage() != null ? e.getMessage() : "Nieznany błąd", e);
        throw new ZegoApiException("Nie można utworzyć spotkania: " +
                (e.getMessage() != null ? e.getMessage() : "Wystąpił nieznany błąd"), e);
    }
}

    /**
     * Rozpoczyna spotkanie
     *
     * @param meetingId ID spotkania
     * @return Zaktualizowane spotkanie
     */
    @Transactional
    public Meeting startMeeting(UUID meetingId) {
        try {
            Meeting meeting = meetingRepository.findById(meetingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono spotkania o ID: " + meetingId));

            meeting.setStatus(Status.ONGOING);
            meeting.setStartTime(LocalDateTime.now());

            Meeting updatedMeeting = meetingRepository.save(meeting);
            logger.info("Rozpoczęto spotkanie: {}", meetingId);

            return updatedMeeting;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Błąd podczas rozpoczynania spotkania: {}", e.getMessage());
            throw new ZegoApiException("Nie można rozpocząć spotkania", e);
        }
    }

    /**
     * Kończy spotkanie
     *
     * @param meetingId ID spotkania
     * @return Zaktualizowane spotkanie
     */
    @Transactional
    public Meeting endMeeting(UUID meetingId) {
        try {
            Meeting meeting = meetingRepository.findById(meetingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono spotkania o ID: " + meetingId));

            meeting.setStatus(Status.COMPLETED);
            meeting.setEndTime(LocalDateTime.now());

            Meeting updatedMeeting = meetingRepository.save(meeting);
            logger.info("Zakończono spotkanie: {}", meetingId);

            return updatedMeeting;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Błąd podczas kończenia spotkania: {}", e.getMessage());
            throw new ZegoApiException("Nie można zakończyć spotkania", e);
        }
    }
}