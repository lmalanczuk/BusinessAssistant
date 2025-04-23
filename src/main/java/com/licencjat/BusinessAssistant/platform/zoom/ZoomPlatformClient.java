package com.licencjat.BusinessAssistant.platform.zoom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.client.zoom.ZoomMeetingClient;
import com.licencjat.BusinessAssistant.client.zoom.ZoomRecordingClient;
import com.licencjat.BusinessAssistant.model.zoom.ZoomMeetingRequest;
import com.licencjat.BusinessAssistant.model.zoom.ZoomMeetingSettings;
import com.licencjat.BusinessAssistant.platform.MeetingPlatformClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZoomPlatformClient implements MeetingPlatformClient {
    private static final Logger logger = LoggerFactory.getLogger(ZoomPlatformClient.class);
    private final ZoomMeetingClient meetingClient;
    private final ZoomRecordingClient recordingClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ZoomPlatformClient(ZoomMeetingClient meetingClient, ZoomRecordingClient recordingClient) {
        this.meetingClient = meetingClient;
        this.recordingClient = recordingClient;
    }

    @Override
    public JsonNode createMeeting(String title, LocalDateTime startTime, int durationMinutes,
                                 String userId, Map<String, Object> options) {
        try {
            // Konwersja parametrów na format specyficzny dla Zoom
            ZoomMeetingSettings.Builder settingsBuilder = ZoomMeetingSettings.builder()
                    .hostVideo(true)
                    .participantVideo(true)
                    .joinBeforeHost(false)
                    .muteUponEntry(true)
                    .autoRecording("cloud")
                    .waitingRoom(true);

            // Dodanie opcjonalnych ustawień, jeśli zostały przekazane
            if (options != null) {
                if (options.containsKey("hostVideo")) {
                    settingsBuilder.hostVideo((Boolean) options.get("hostVideo"));
                }
                if (options.containsKey("participantVideo")) {
                    settingsBuilder.participantVideo((Boolean) options.get("participantVideo"));
                }
                if (options.containsKey("joinBeforeHost")) {
                    settingsBuilder.joinBeforeHost((Boolean) options.get("joinBeforeHost"));
                }
                if (options.containsKey("muteUponEntry")) {
                    settingsBuilder.muteUponEntry((Boolean) options.get("muteUponEntry"));
                }
                if (options.containsKey("autoRecording")) {
                    settingsBuilder.autoRecording((String) options.get("autoRecording"));
                }
                if (options.containsKey("waitingRoom")) {
                    settingsBuilder.waitingRoom((Boolean) options.get("waitingRoom"));
                }
            }

            ZoomMeetingRequest meetingRequest = ZoomMeetingRequest.builder()
                    .topic(title)
                    .type(2) // Scheduled meeting
                    .startTime(startTime.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
                    .duration(durationMinutes)
                    .timezone("UTC")
                    .settings(settingsBuilder.build())
                    .build();

            Map<String, Object> meetingDetails = objectMapper.convertValue(meetingRequest, Map.class);

            return meetingClient.createMeeting(meetingDetails, userId);
        } catch (Exception e) {
            logger.error("Błąd podczas tworzenia spotkania w Zoom: {}", e.getMessage());
            throw new RuntimeException("Błąd podczas tworzenia spotkania Zoom", e);
        }
    }

    @Override
    public JsonNode getMeeting(String meetingId) {
        return meetingClient.getMeeting(meetingId);
    }

    @Override
    public JsonNode updateMeeting(String meetingId, Map<String, Object> updateData) {
        return meetingClient.updateMeeting(meetingId, updateData);
    }

    @Override
    public JsonNode startMeeting(String meetingId) {
        return meetingClient.startMeeting(meetingId);
    }

    @Override
    public JsonNode endMeeting(String meetingId) {
        return meetingClient.endMeeting(meetingId);
    }

    @Override
    public JsonNode getMeetingParticipants(String meetingId) {
        return meetingClient.getMeetingParticipants(meetingId);
    }

    @Override
    public JsonNode inviteToMeeting(String meetingId, List<String> emails) {
        return meetingClient.inviteToMeeting(meetingId, emails);
    }

    @Override
    public JsonNode getMeetingRecordings(String meetingId) {
        return recordingClient.getMeetingRecordings(meetingId);
    }
}