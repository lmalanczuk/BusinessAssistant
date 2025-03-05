package com.licencjat.BusinessAssistant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.licencjat.BusinessAssistant.model.response.ZoomMeetingResponse;
import com.licencjat.BusinessAssistant.model.response.ZoomTokenResponse;
import com.licencjat.BusinessAssistant.service.ZoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/zoom")
public class ZoomController {

    private final ZoomService zoomService;

    @Autowired
    public ZoomController(ZoomService zoomService) {
        this.zoomService = zoomService;
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<ZoomTokenResponse> handleOAuthCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String userId) {
        ZoomTokenResponse tokenResponse = zoomService.handleOAuthCallback(code, userId);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/meetings")
    public ResponseEntity<ZoomMeetingResponse> createMeeting(
            @RequestParam("userId") UUID userId,
            @RequestBody Map<String, Object> meetingDetails) {

        String title = (String) meetingDetails.get("title");
        LocalDateTime startTime = LocalDateTime.parse((String) meetingDetails.get("startTime"));
        int duration = Integer.parseInt(meetingDetails.get("durationMinutes").toString());

        ZoomMeetingResponse response = zoomService.createZoomMeeting(userId, title, startTime, duration);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/meetings")
    public ResponseEntity<JsonNode> listMeetings(@RequestParam("userId") UUID userId) {
        JsonNode meetings = zoomService.listUserMeetings(userId);
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/meetings/{meetingId}/recordings")
    public ResponseEntity<JsonNode> getMeetingRecordings(
            @RequestParam("userId") UUID userId,
            @PathVariable("meetingId") String meetingId) {
        JsonNode recordings = zoomService.getMeetingRecordings(userId, meetingId);
        return ResponseEntity.ok(recordings);
    }
}