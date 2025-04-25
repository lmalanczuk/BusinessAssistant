package com.licencjat.BusinessAssistant.client.zoom;

import com.fasterxml.jackson.databind.JsonNode;

public interface ZoomRecordingClient {
    JsonNode getUserRecordings(String userId, String from, String to);
    JsonNode getMeetingRecordings(String meetingId);
}