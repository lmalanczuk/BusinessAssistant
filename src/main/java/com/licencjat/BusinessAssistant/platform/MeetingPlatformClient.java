package com.licencjat.BusinessAssistant.platform;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Common interface for meeting platform clients
 */
public interface MeetingPlatformClient {
    /**
     * Creates a new meeting on the platform
     */
    JsonNode createMeeting(String title, LocalDateTime startTime, int durationMinutes,
                           String userId, Map<String, Object> options);

    /**
     * Gets meeting details
     */
    JsonNode getMeeting(String meetingId);

    /**
     * Updates a meeting
     */
    JsonNode updateMeeting(String meetingId, Map<String, Object> updateData);

    /**
     * Starts a meeting
     */
    JsonNode startMeeting(String meetingId);

    /**
     * Ends a meeting
     */
    JsonNode endMeeting(String meetingId);

    /**
     * Gets list of meeting participants
     */
    JsonNode getMeetingParticipants(String meetingId);

    /**
     * Sends invitations to a meeting
     */
    JsonNode inviteToMeeting(String meetingId, List<String> emails);

    /**
     * Gets recordings for a meeting
     */
    JsonNode getMeetingRecordings(String meetingId);

    /**
     * Gets recordings for a user in a given date range
     * Note: This method might not be supported by all platforms
     * @param userId The platform-specific user ID
     * @param from Start date in format YYYY-MM-DD
     * @param to End date in format YYYY-MM-DD
     * @return Recordings data as JsonNode
     * @throws UnsupportedOperationException if not supported by the platform
     */
    default JsonNode getUserRecordings(String userId, String from, String to) {
        throw new UnsupportedOperationException("Getting user recordings is not supported by this platform");
    }
}