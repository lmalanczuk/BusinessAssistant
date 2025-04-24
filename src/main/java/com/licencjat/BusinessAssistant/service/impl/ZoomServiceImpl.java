package com.licencjat.BusinessAssistant.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.client.zoom.ZoomTokenManager;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.entity.enums.Platform;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import com.licencjat.BusinessAssistant.exception.AuthenticationException;
import com.licencjat.BusinessAssistant.exception.ResourceNotFoundException;
import com.licencjat.BusinessAssistant.exception.ZoomApiException;
import com.licencjat.BusinessAssistant.factory.MeetingPlatformFactory;
import com.licencjat.BusinessAssistant.model.zoom.webhook.ZoomWebhookEvent;
import com.licencjat.BusinessAssistant.platform.MeetingPlatformClient;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.service.WebhookHandlerRegistry;
import com.licencjat.BusinessAssistant.service.ZoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ZoomServiceImpl implements ZoomService {
    private static final Logger logger = LoggerFactory.getLogger(ZoomServiceImpl.class);

    private final MeetingPlatformFactory platformFactory;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final WebhookHandlerRegistry webhookHandlerRegistry;
    private final ZoomTokenManager tokenManager;

    @Autowired
    public ZoomServiceImpl(
            MeetingPlatformFactory platformFactory,
            MeetingRepository meetingRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            WebhookHandlerRegistry webhookHandlerRegistry,
            ZoomTokenManager tokenManager) {
        this.platformFactory = platformFactory;
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.webhookHandlerRegistry = webhookHandlerRegistry;
        this.tokenManager = tokenManager;
    }

    @Override
    public void processWebhook(Map<String, Object> webhookData) {
        try {
            // Extract event type
            ZoomWebhookEvent event = objectMapper.convertValue(webhookData, ZoomWebhookEvent.class);
            String eventType = event.getEvent();
            logger.info("Processing webhook event: {}", eventType);

            // Delegate to handler registry
            webhookHandlerRegistry.processWebhook(eventType, webhookData);
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            throw new RuntimeException("Error processing webhook", e);
        }
    }

    @Override
    public void downloadRecordingsForMeeting(String meetingId) {
        try {
            // Find meeting in database
            Optional<Meeting> meetingOpt = meetingRepository.findByZoomMeetingId(meetingId);
            if (!meetingOpt.isPresent()) {
                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
            }

            Meeting meeting = meetingOpt.get();

            // Get platform client
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Get recordings
            JsonNode recordings = platformClient.getMeetingRecordings(meetingId);

            // Further processing handled by a service that would download the recordings
            // This is a placeholder for the actual implementation
            logger.info("Found {} recordings for meeting: {}",
                    recordings.has("recording_files") ? recordings.get("recording_files").size() : 0,
                    meetingId);

            // The actual downloading process would be handled by ZoomRecordingCompletedHandler
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error downloading recordings for meeting: {}", meetingId, e);
            throw new RuntimeException("Failed to download recordings", e);
        }
    }

    @Override
    @Transactional
    public Meeting startMeeting(String meetingId) {
        try {
            // Find meeting in database
            Optional<Meeting> meetingOptional = meetingRepository.findByZoomMeetingId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Get platform client
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Call platform API to start meeting
            platformClient.startMeeting(meetingId);

            // Update meeting status
            meeting.setStatus(Status.ONGOING);
            meeting.setStartTime(LocalDateTime.now());

            return meetingRepository.save(meeting);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error starting meeting: {}", e.getMessage());
            throw new ZoomApiException("Cannot start meeting", e);
        }
    }

    @Override
    @Transactional
    public Meeting endMeeting(String meetingId) {
        try {
            // Find meeting in database
            Optional<Meeting> meetingOptional = meetingRepository.findByZoomMeetingId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Get platform client
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Call platform API to end meeting
            platformClient.endMeeting(meetingId);

            // Update meeting status
            meeting.setStatus(Status.COMPLETED);
            meeting.setEndTime(LocalDateTime.now());

            return meetingRepository.save(meeting);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error ending meeting: {}", e.getMessage());
            throw new ZoomApiException("Cannot end meeting", e);
        }
    }

    @Override
    public JsonNode getMeetingParticipants(String meetingId) {
        try {
            // Check if meeting exists in database
            Optional<Meeting> meetingOptional = meetingRepository.findByZoomMeetingId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Get platform client
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Call platform API to get participants
            return platformClient.getMeetingParticipants(meetingId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting meeting participants: {}", e.getMessage());
            throw new ZoomApiException("Cannot get meeting participants", e);
        }
    }

    @Override
    public JsonNode inviteToMeeting(String meetingId, List<String> emails) {
        try {
            // Check if meeting exists in database
            Optional<Meeting> meetingOptional = meetingRepository.findByZoomMeetingId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Get platform client
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Call platform API to send invitations
            return platformClient.inviteToMeeting(meetingId, emails);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error inviting to meeting: {}", e.getMessage());
            throw new ZoomApiException("Cannot send invitations", e);
        }
    }

    @Override
    @Transactional
    public Meeting updateMeeting(String meetingId, Map<String, Object> updateData) {
        try {
            // Find meeting in database
            Optional<Meeting> meetingOptional = meetingRepository.findByZoomMeetingId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Get platform client
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Call platform API to update meeting
            JsonNode updatedMeetingData = platformClient.updateMeeting(meetingId, updateData);

            // Update meeting data in database
            if (updateData.containsKey("topic")) {
                meeting.setTitle((String) updateData.get("topic"));
            }

            if (updateData.containsKey("start_time")) {
                String startTimeStr = (String) updateData.get("start_time");
                // Proper parsing of ISO date-time string
                meeting.setStartTime(LocalDateTime.parse(startTimeStr.replace("Z", "")));
            }

            if (updateData.containsKey("duration")) {
                Integer durationMinutes = (Integer) updateData.get("duration");
                meeting.setEndTime(meeting.getStartTime().plusMinutes(durationMinutes));
            }

            return meetingRepository.save(meeting);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating meeting: {}", e.getMessage());
            throw new ZoomApiException("Cannot update meeting", e);
        }
    }

    @Override
    @Transactional
    public Meeting createZoomMeeting(String title, LocalDateTime startTime, int durationMinutes, UUID hostUserId) {
        try {
            Optional<Users> hostOpt = userRepository.findById(hostUserId);
            if (!hostOpt.isPresent()) {
                logger.error("User not found with ID: {}", hostUserId);
                throw new ResourceNotFoundException("User not found with ID: " + hostUserId);
            }

            Users host = hostOpt.get();

            if (host.getZoomUserId() == null || host.getZoomAccessToken() == null) {
                logger.error("User {} is not connected with Zoom", hostUserId);
                throw new IllegalArgumentException("User is not connected with Zoom. Please connect your Zoom account first.");
            }

            // Check if user's Zoom token is expired and should be refreshed
            if (host.getZoomTokenExpiry() != null && host.getZoomTokenExpiry().isBefore(LocalDateTime.now())) {
                logger.info("Zoom token expired for user {}, refreshing", hostUserId);
                try {
                    refreshUserZoomToken(host);
                } catch (AuthenticationException e) {
                    logger.error("Failed to refresh Zoom token for user {}", hostUserId, e);
                    throw new IllegalArgumentException("Your Zoom authorization has expired. Please reconnect your Zoom account.");
                }
            }

            // Get platform client
            MeetingPlatformClient platformClient = platformFactory.getClient("ZOOM");

            // Prepare additional options
            Map<String, Object> options = new HashMap<>();
            options.put("hostVideo", true);
            options.put("participantVideo", true);
            options.put("joinBeforeHost", false);
            options.put("muteUponEntry", true);
            options.put("autoRecording", "cloud");
            options.put("waitingRoom", true);

            // Create meeting with retry logic
            JsonNode response;
            try {
                response = platformClient.createMeeting(
                        title, startTime, durationMinutes, host.getZoomUserId(), options);
            } catch (ZoomApiException e) {
                // Handle specific Zoom API errors
                if (e.getMessage().contains("User is not found or does not belong to this account")) {
                    logger.error("Invalid Zoom user ID: {}", host.getZoomUserId());
                    throw new IllegalArgumentException("Your Zoom account is not valid. Please reconnect your Zoom account.");
                } else if (e.getMessage().contains("Validation Failed") || e.getMessage().contains("Invalid parameter")) {
                    logger.error("Invalid parameters for Zoom meeting: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid meeting parameters: " + e.getMessage());
                } else if (e.getMessage().contains("Too many requests")) {
                    logger.error("Rate limit exceeded for Zoom API");
                    throw new IllegalArgumentException("Zoom rate limit exceeded. Please try again in a few minutes.");
                } else {
                    logger.error("Error creating Zoom meeting", e);
                    throw new RuntimeException("Failed to create Zoom meeting: " + e.getMessage(), e);
                }
            }

            // Create meeting entity
            Meeting meeting = new Meeting();
            meeting.setTitle(title);
            meeting.setStartTime(startTime);
            meeting.setEndTime(startTime.plusMinutes(durationMinutes));
            meeting.setStatus(Status.PLANNED);
            meeting.setPlatform(Platform.ZOOM);
            meeting.setZoomHostId(host.getZoomUserId());

            // Check if required fields are present in the response
            if (response.has("join_url")) {
                meeting.setZoomJoinUrl(response.get("join_url").asText());
            } else {
                logger.warn("Join URL not found in Zoom response");
                meeting.setZoomJoinUrl("URL not available");
            }

            if (response.has("id")) {
                meeting.setZoomMeetingId(response.get("id").asText());
            } else {
                logger.error("Meeting ID not found in Zoom response");
                throw new RuntimeException("Meeting ID not returned by Zoom API");
            }

            // Add host as a participant
            Set<Users> participants = new HashSet<>();
            participants.add(host);
            meeting.setParticipants(participants);

            Meeting savedMeeting = meetingRepository.save(meeting);
            logger.info("Successfully created Zoom meeting: {}", savedMeeting.getId());

            return savedMeeting;
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            // These exceptions are already properly formatted, so just rethrow
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating Zoom meeting", e);
            throw new RuntimeException("Error creating Zoom meeting: " + e.getMessage(), e);
        }
    }

    @Override
    public JsonNode getMeeting(String meetingId) {
        try {
            // Check if meeting exists in database
            Optional<Meeting> meetingOptional = meetingRepository.findByZoomMeetingId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Get platform client
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Call platform API to get meeting details
            return platformClient.getMeeting(meetingId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting meeting details: {}", e.getMessage());
            throw new ZoomApiException("Cannot get meeting details", e);
        }
    }

    @Override
    public JsonNode getMeetingRecordings(String meetingId) {
        try {
            // Check if meeting exists in database
            Optional<Meeting> meetingOptional = meetingRepository.findByZoomMeetingId(meetingId);

            if (!meetingOptional.isPresent()) {
                throw new ResourceNotFoundException("Meeting not found with ID: " + meetingId);
            }

            Meeting meeting = meetingOptional.get();

            // Get platform client
            MeetingPlatformClient platformClient = platformFactory.getClient(meeting.getPlatform().name());

            // Call platform API to get meeting recordings
            return platformClient.getMeetingRecordings(meetingId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting meeting recordings: {}", e.getMessage());
            throw new ZoomApiException("Cannot get meeting recordings", e);
        }
    }

    @Override
    public JsonNode getUserRecordings(String userId, String from, String to) {
        try {
        // Validate user exists
        Optional<Users> userOpt = userRepository.findByZoomUserId(userId);
        if (!userOpt.isPresent()) {
            throw new ResourceNotFoundException("User not found with Zoom ID: " + userId);
        }

        // Get platform client
        MeetingPlatformClient platformClient = platformFactory.getClient("ZOOM");

        // Call platform method to get user recordings
        return platformClient.getUserRecordings(userId, from, to);
    } catch (ResourceNotFoundException e) {
        throw e;
    } catch (UnsupportedOperationException e) {
        logger.error("Getting user recordings not supported by platform", e);
        throw new ZoomApiException("Getting user recordings is not supported", e);
    } catch (Exception e) {
        logger.error("Error getting user recordings: {}", e.getMessage());
        throw new ZoomApiException("Cannot get user recordings", e);
    }
}

    /**
     * Helper method to refresh a user's Zoom access token
     */
    private void refreshUserZoomToken(Users user) {
        try {
            // Use the ZoomTokenManager to refresh the token
            // First set the current tokens
            tokenManager.setTokens(
                user.getZoomAccessToken(),
                user.getZoomRefreshToken(),
                ChronoUnit.SECONDS.between(LocalDateTime.now(), user.getZoomTokenExpiry())
            );

            // Refresh the token
            tokenManager.refreshAccessToken();

            // Get the new tokens and update the user
            user.setZoomAccessToken(tokenManager.getAccessToken());
            user.setZoomRefreshToken(tokenManager.getRefreshToken());
            user.setZoomTokenExpiry(LocalDateTime.now().plusSeconds(3600)); // Default to 1 hour if not specified

            userRepository.save(user);
            logger.info("Successfully refreshed Zoom token for user: {}", user.getId());
        } catch (Exception e) {
            logger.error("Failed to refresh Zoom token", e);
            throw new AuthenticationException("Failed to refresh Zoom token", e);
        }
    }
}