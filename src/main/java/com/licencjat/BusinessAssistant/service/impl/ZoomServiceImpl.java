package com.licencjat.BusinessAssistant.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.licencjat.BusinessAssistant.client.ZoomClient;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.entity.enums.Platform;
import com.licencjat.BusinessAssistant.entity.enums.Status;

import com.licencjat.BusinessAssistant.model.ZoomMeetingSettings;
import com.licencjat.BusinessAssistant.model.request.ZoomMeetingRequest;
import com.licencjat.BusinessAssistant.model.response.ZoomMeetingResponse;
import com.licencjat.BusinessAssistant.model.response.ZoomTokenResponse;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.service.ZoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ZoomServiceImpl implements ZoomService {

    private static final Logger logger = LoggerFactory.getLogger(ZoomServiceImpl.class);
    private final ZoomClient zoomClient;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;

    @Autowired
    public ZoomServiceImpl(ZoomClient zoomClient, UserRepository userRepository, MeetingRepository meetingRepository) {
        this.zoomClient = zoomClient;
        this.userRepository = userRepository;
        this.meetingRepository = meetingRepository;
    }

    @Override
    public ZoomTokenResponse handleOAuthCallback(String code, String userId) {
        try {
            ZoomTokenResponse tokenResponse = zoomClient.getAccessToken(code);

            // Znajdź użytkownika
            Optional<Users> userOpt = userRepository.findById(UUID.fromString(userId));
            if (userOpt.isPresent()) {
                Users user = userOpt.get();

                // Zapisz tokeny i czas wygaśnięcia
                user.setZoomAccessToken(tokenResponse.getAccessToken());
                user.setZoomRefreshToken(tokenResponse.getRefreshToken());
                user.setZoomTokenExpiry(
                    LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn())
                );

                userRepository.save(user);
            }

            return tokenResponse;
        } catch (Exception e) {
            logger.error("Error handling OAuth callback: {}", e.getMessage());
            throw new RuntimeException("Failed to exchange OAuth code for tokens", e);
        }
    }

    @Override
    public ZoomTokenResponse refreshTokenIfNeeded(UUID userId) {
        Optional<Users> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();

            LocalDateTime tokenExpiry = user.getZoomTokenExpiry();
            if (tokenExpiry != null && LocalDateTime.now().isAfter(tokenExpiry.minusMinutes(5))) {
                ZoomTokenResponse refreshedToken = zoomClient.refreshAccessToken(user.getZoomRefreshToken());

                user.setZoomAccessToken(refreshedToken.getAccessToken());
                user.setZoomRefreshToken(refreshedToken.getRefreshToken());
                user.setZoomTokenExpiry(
                    LocalDateTime.now().plusSeconds(refreshedToken.getExpiresIn())
                );

                userRepository.save(user);
                return refreshedToken;
            }
        }
        return null;
    }

    @Override
    public ZoomMeetingResponse createZoomMeeting(UUID userId, String title, LocalDateTime startTime, int durationMinutes) {
        Optional<Users> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        Users user = userOpt.get();
        refreshTokenIfNeeded(userId);

        ZoomMeetingRequest meetingRequest = new ZoomMeetingRequest();
        meetingRequest.setTopic(title);
        meetingRequest.setType("2"); // scheduled meeting

        // Konwersja do formatu ISO-8601 wymaganego przez Zoom
        String formattedStartTime = startTime.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        meetingRequest.setStart_time(formattedStartTime);

        meetingRequest.setDuration(durationMinutes);
        meetingRequest.setTimezone(ZoneId.systemDefault().getId());

        // Konfiguracja nagrywania
        ZoomMeetingSettings settings = new ZoomMeetingSettings();
        settings.setAutoRecording("cloud");
        meetingRequest.setSettings(settings);

        ZoomMeetingResponse zoomResponse = zoomClient.createMeeting(
            user.getZoomAccessToken(),
            meetingRequest
        );

        // Zapisywanie spotkania w bazie danych
        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setStartTime(startTime);
        meeting.setEndTime(startTime.plusMinutes(durationMinutes));
        meeting.setStatus(Status.PLANNED);
        meeting.setPlatform(Platform.ZOOM);
        meeting.setZoomMeetingId(zoomResponse.getId());
        meeting.setZoomHostId(zoomResponse.getHostId());
        meeting.setZoomJoinUrl(zoomResponse.getJoinUrl());

        meetingRepository.save(meeting);

        return zoomResponse;
    }

    @Override
    public JsonNode listUserMeetings(UUID userId) {
        Optional<Users> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        Users user = userOpt.get();
        refreshTokenIfNeeded(userId);

        return zoomClient.listMeetings(user.getZoomAccessToken());
    }

    @Override
    public JsonNode getMeetingRecordings(UUID userId, String meetingId) {
        Optional<Users> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }

        Users user = userOpt.get();
        refreshTokenIfNeeded(userId);

        return zoomClient.getMeetingRecordings(user.getZoomAccessToken(), meetingId);
    }
}