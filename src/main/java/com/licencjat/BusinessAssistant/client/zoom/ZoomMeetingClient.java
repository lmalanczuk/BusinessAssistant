//package com.licencjat.BusinessAssistant.client.zoom;
//
//import com.fasterxml.jackson.databind.JsonNode;
//
//import java.util.Map;
//
//public interface ZoomMeetingClient {
//    JsonNode getMeeting(String meetingId);
//    JsonNode createMeeting(Map<String, Object> meetingDetails, String userId);
//    JsonNode updateMeeting(String meetingId, Map<String, Object> updateData);
//    JsonNode startMeeting(String meetingId);
//    JsonNode endMeeting(String meetingId);
//    JsonNode getMeetingParticipants(String meetingId);
//    JsonNode inviteToMeeting(String meetingId, java.util.List<String> emails);
//}