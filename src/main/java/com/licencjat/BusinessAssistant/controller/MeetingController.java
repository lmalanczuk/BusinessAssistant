package com.licencjat.BusinessAssistant.controller;

import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import com.licencjat.BusinessAssistant.model.request.CreateMeetingRequest;
import com.licencjat.BusinessAssistant.model.request.JoinMeetingRequest;
import com.licencjat.BusinessAssistant.model.response.MeetingTokenResponse;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.security.UserPrincipal;
import com.licencjat.BusinessAssistant.service.DailyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final DailyService dailyService;
    private final MeetingRepository meetingRepository;

    public MeetingController(DailyService dailyService, MeetingRepository meetingRepository) {
        this.dailyService = dailyService;
        this.meetingRepository = meetingRepository;
    }

    /** 1) „Start” – tworzy nowy pokój i zwraca token właściciela */
    @PostMapping("/start")
    public ResponseEntity<MeetingTokenResponse> startMeeting(
            @RequestBody CreateMeetingRequest req,
            @AuthenticationPrincipal(expression = "id") UUID hostUserId
    ) {
        MeetingTokenResponse resp = dailyService.createAndJoinRoom(
            req.getTitle(),
            req.getStartTime(),
            req.getDurationMinutes(),
            hostUserId.toString(),
            /* isOwner = */ true
        );
        return ResponseEntity.ok(resp);
    }

    /** 2) „Join” – generuje token uczestnika do istniejącego pokoju */
    @PostMapping("/join")
    public ResponseEntity<MeetingTokenResponse> joinMeeting(
            @RequestBody JoinMeetingRequest req,
            @AuthenticationPrincipal(expression = "firstName") String userName
    ) {
        MeetingTokenResponse resp = dailyService.generateParticipantToken(
            req.getRoomName(),
            userName,
            /* isOwner = */ false
        );
        return ResponseEntity.ok(resp);
    }

    /** 3) „Schedule” – planuje pokój z nbf/exp i zwraca token właściciela */
    @PostMapping("/schedule")
    public ResponseEntity<MeetingTokenResponse> scheduleMeeting(
            @RequestBody CreateMeetingRequest req,
            @AuthenticationPrincipal(expression = "id") UUID hostUserId
    ) {
        MeetingTokenResponse resp = dailyService.scheduleRoomAndToken(
            req.getTitle(),
            req.getStartTime(),
            req.getDurationMinutes(),
            hostUserId.toString(),
            /* isOwner = */ true
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/current")
    public ResponseEntity<Meeting> getCurrentMeeting() {
        LocalDateTime now = LocalDateTime.now();
        return meetingRepository
            .findByStatusAndStartTimeBeforeAndEndTimeAfter(Status.ONGOING, now, now)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Meeting>> getUpcomingMeetings() {
        LocalDateTime now = LocalDateTime.now();
        return ResponseEntity.ok(
            meetingRepository.findByStartTimeAfterOrderByStartTimeAsc(now)
        );
    }

}
