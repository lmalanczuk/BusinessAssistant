package com.licencjat.BusinessAssistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licencjat.BusinessAssistant.config.DailyConfig;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Summary;
import com.licencjat.BusinessAssistant.entity.Transcription;
import com.licencjat.BusinessAssistant.entity.enums.Platform;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import com.licencjat.BusinessAssistant.model.request.SummaryRequest;
import com.licencjat.BusinessAssistant.model.response.MeetingTokenResponse;
import com.licencjat.BusinessAssistant.model.response.SummaryResponse;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.repository.SummaryRepository;
import com.licencjat.BusinessAssistant.repository.TranscriptionRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class DailyService {
    private final DailyConfig cfg;
    private final RestTemplate rt;
    private final ObjectMapper om;
    private final MeetingRepository meetingRepo;
    private final TranscriptionRepository transcriptionRepo;
    private final SummarizationService summarizationService;
    private final SummaryRepository summaryRepo;

    public DailyService(DailyConfig cfg, RestTemplate rt, ObjectMapper om, MeetingRepository meetingRepo, TranscriptionRepository transcriptionRepo, SummarizationService summarizationService, SummaryRepository summaryRepo) {
        this.cfg = cfg;
        this.rt = rt;
        this.om = om;
        this.meetingRepo = meetingRepo;
        this.transcriptionRepo = transcriptionRepo;
        this.summarizationService = summarizationService;
        this.summaryRepo = summaryRepo;
    }

    /** 1) Start: tworzy pokój + token dla właściciela */
    @Transactional
    public MeetingTokenResponse createAndJoinRoom(
            String title,
            LocalDateTime start,
            int duration,
            String hostUserId,
            boolean isOwner
    ) {
        // 1. Utwórz pokój
        String roomName = "meet-" + UUID.randomUUID().toString().substring(0,8);
        Map<String,Object> roomConfig = Map.of(
            "name", roomName,
            "privacy", "public",
            "properties", Map.of(
                "enable_chat", true,
                "lang", "pl",
                "exp", start.plusMinutes(duration).toEpochSecond(ZoneOffset.UTC)
            )
        );
        HttpEntity<Map<String,Object>> createReq = new HttpEntity<>(roomConfig, makeAuthHeaders());
        JsonNode roomData = rt.exchange(
                cfg.getApiUrl() + "/rooms",
                HttpMethod.POST, createReq, JsonNode.class
        ).getBody();

        // 2. Zapisz spotkanie w bazie
        Meeting m = new Meeting();
        m.setTitle(title);
        m.setStartTime(start);
        m.setEndTime(start.plusMinutes(duration));
        m.setStatus(Status.ONGOING);
        m.setPlatform(Platform.DAILY);
        m.setDailyRoomName(roomName);
        m.setDailyRoomUrl(roomData.get("url").asText());
        meetingRepo.save(m);

        // 3. Generuj token
        String token = generateMeetingToken(roomName, hostUserId, isOwner, true);

        return new MeetingTokenResponse(token, roomData.get("url").asText(), roomName);
    }

    /** 2) Join: generuje token dla uczestnika do istniejącego pokoju */
    public MeetingTokenResponse generateParticipantToken(
            String roomName,
            String userName,
            boolean isOwner
    ) {
        // 1. Sprawdź pokój — opcjonalne: GET /rooms/{roomName}
        // 2. Generuj token
        String token = generateMeetingToken(roomName, userName, isOwner, false);
        String url = String.format("https://%s.daily.co/%s", cfg.getDomain(), roomName);
        return new MeetingTokenResponse(token, url, roomName);
    }

    /** 3) Schedule: planuje pokój z nbf/exp i od razu zwraca token właściciela */
    @Transactional
    public MeetingTokenResponse scheduleRoomAndToken(
            String title,
            LocalDateTime start,
            int duration,
            String hostUserId,
            boolean isOwner
    ) {
        String roomName = "meet-" + UUID.randomUUID().toString().substring(0,8);
        // ustawiamy zarówno nbf, jak i exp
        Map<String,Object> props = new HashMap<>();
        props.put("nbf", start.toEpochSecond(ZoneOffset.UTC));
        props.put("exp", start.plusMinutes(duration).toEpochSecond(ZoneOffset.UTC));
        props.put("privacy", "private");

        Map<String,Object> cfgRoom = Map.of("name", roomName, "properties", props);
        HttpEntity<Map<String,Object>> req = new HttpEntity<>(cfgRoom, makeAuthHeaders());
        JsonNode roomData = rt.exchange(
                cfg.getApiUrl() + "/rooms",
                HttpMethod.POST, req, JsonNode.class
        ).getBody();

        // Zapisz do bazy jako PLANNED
        Meeting m = new Meeting();
        m.setTitle(title);
        m.setStartTime(start);
        m.setEndTime(start.plusMinutes(duration));
        m.setStatus(Status.PLANNED);
        m.setPlatform(Platform.DAILY);
        m.setDailyRoomName(roomName);
        m.setDailyRoomUrl(roomData.get("url").asText());
        meetingRepo.save(m);

        // Generuj token
        String token = generateMeetingToken(roomName, hostUserId, isOwner, true);

        return new MeetingTokenResponse(token, roomData.get("url").asText(), roomName);
    }

    /** Wspólna logika generowania tokenu */
    private String generateMeetingToken(
            String roomName,
            String userName,
            boolean isOwner,
            boolean autoTranscribe
    ) {
        String url = cfg.getApiUrl() + "/meeting-tokens";
        HttpHeaders h = makeAuthHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> props = new HashMap<>();
        props.put("room_name", roomName);
        props.put("user_name", userName);
        props.put("is_owner", isOwner);
        if (autoTranscribe) {
            props.put("auto_start_transcription", true);
        }

        Map<String,Object> body = Map.of("properties", props);
        HttpEntity<Map<String,Object>> e = new HttpEntity<>(body, h);
        JsonNode resp = rt.exchange(url, HttpMethod.POST, e, JsonNode.class).getBody();
        return resp.get("token").asText();
    }
    /**
     * Obsługa zdarzeń webhooka Daily.co
     * @param event cała paczka JSON od Daily
     */
    @Transactional
    public void handleWebhook(Map<String,Object> event) {
        String eventType = (String) event.get("event");
        Map<String,Object> payload = (Map<String,Object>) event.get("payload");

        switch (eventType) {
            case "transcript.ready-to-download":
                processTranscript(payload);
                break;
            case "recording.ready-to-download":
                processRecording(payload);
                break;
            default:
                // inne zdarzenia możesz pominąć
        }
    }

    private void processTranscript(Map<String, Object> payload) {
    String transcriptId = (String) payload.get("id");
    String roomName     = (String) payload.get("room_name");

    // 1) Znajdź spotkanie
    Meeting meeting = meetingRepo.findByDailyRoomName(roomName)
        .orElseThrow(() -> new RuntimeException("Meeting not found"));

    // 2) Pobierz transkrypt
    String url = String.format("%s/transcript/%s", cfg.getApiUrl(), transcriptId);
    HttpEntity<Void> req = new HttpEntity<>(makeAuthHeaders());
    JsonNode resp = rt.exchange(url, HttpMethod.GET, req, JsonNode.class).getBody();
    String text = resp.path("text").asText();

    // 3) Zapisz Transcription
    Transcription t = new Transcription();
    t.setMeetingId(meeting);
    t.setTranscriptionText(text);
    t.setGeneratedAt(LocalDateTime.now());
    transcriptionRepo.save(t);

    // 4) Generuj summary i zapisz
    SummaryRequest sr = new SummaryRequest();
    sr.setText(text);

    SummaryResponse summaryResp = summarizationService.generateSummaryFromText(sr);

    Summary summary = new Summary();
    summary.setMeetingId(meeting);
    summary.setGeneratedSummary(summaryResp.getSummaryText());
    summary.setGeneratedAt(LocalDateTime.now());
    summaryRepo.save(summary);
}

    private void processRecording(Map<String,Object> payload) {
        String accessLink = (String) payload.get("access_link");
        String roomName   = (String) payload.get("room_name");

        meetingRepo.findByDailyRoomName(roomName).ifPresent(m -> {
            m.setRecordingUrl(accessLink);
            meetingRepo.save(m);
        });
    }

    private HttpHeaders makeAuthHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(cfg.getApiKey());
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
