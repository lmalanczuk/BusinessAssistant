package com.licencjat.BusinessAssistant.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.licencjat.BusinessAssistant.entity.Meeting;
import com.licencjat.BusinessAssistant.entity.Transcription;
import com.licencjat.BusinessAssistant.entity.enums.Status;
import com.licencjat.BusinessAssistant.model.request.SummaryRequest;
import com.licencjat.BusinessAssistant.repository.MeetingRepository;
import com.licencjat.BusinessAssistant.repository.TranscriptionRepository;
import com.licencjat.BusinessAssistant.service.SummarizationService;
import com.licencjat.BusinessAssistant.service.ZoomWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ZoomWebhookServiceImpl implements ZoomWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(ZoomWebhookServiceImpl.class);
    private final MeetingRepository meetingRepository;
    private final TranscriptionRepository transcriptionRepository;
    private final SummarizationService summarizationService;
    private final AudioTranscriptionService audioTranscriptionService;
    private final RestTemplate restTemplate;

    @Autowired
    public ZoomWebhookServiceImpl(
            MeetingRepository meetingRepository,
            TranscriptionRepository transcriptionRepository,
            SummarizationService summarizationService,
            AudioTranscriptionService audioTranscriptionService,
            RestTemplate restTemplate) {
        this.meetingRepository = meetingRepository;
        this.transcriptionRepository = transcriptionRepository;
        this.summarizationService = summarizationService;
        this.audioTranscriptionService = audioTranscriptionService;
        this.restTemplate = restTemplate;
    }

    @Override
    public void handleMeetingStarted(JsonNode webhookData) {
        String meetingId = webhookData.path("payload").path("object").path("id").asText();

        Optional<Meeting> meetingOpt = findMeetingByZoomId(meetingId);
        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();
            meeting.setStatus(Status.ONGOING);
            meetingRepository.save(meeting);
            logger.info("Meeting started: {}", meetingId);
        } else {
            logger.warn("Received start event for unknown meeting: {}", meetingId);
        }
    }

    @Override
    public void handleMeetingEnded(JsonNode webhookData) {
        String meetingId = webhookData.path("payload").path("object").path("id").asText();

        Optional<Meeting> meetingOpt = findMeetingByZoomId(meetingId);
        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();
            meeting.setStatus(Status.COMPLETED);
            meeting.setEndTime(LocalDateTime.now());
            meetingRepository.save(meeting);
            logger.info("Meeting ended: {}", meetingId);
        } else {
            logger.warn("Received end event for unknown meeting: {}", meetingId);
        }
    }

    @Override
    public void handleRecordingCompleted(JsonNode webhookData) {
        try {
            String meetingId = webhookData.path("payload").path("object").path("meeting_id").asText();

            Optional<Meeting> meetingOpt = findMeetingByZoomId(meetingId);
            if (!meetingOpt.isPresent()) {
                logger.warn("Recording completed for unknown meeting: {}", meetingId);
                return;
            }

            Meeting meeting = meetingOpt.get();

            JsonNode recordingFiles = webhookData.path("payload").path("object").path("recording_files");
            String audioDownloadUrl = null;

            for (JsonNode file : recordingFiles) {
                if (file.path("file_type").asText().equals("M4A") ||
                    file.path("file_type").asText().equals("MP4A")) {
                    audioDownloadUrl = file.path("download_url").asText();
                    break;
                }
            }

            if (audioDownloadUrl == null) {
                logger.warn("No audio recording found for meeting: {}", meetingId);
                return;
            }

            byte[] audioBytes = restTemplate.getForObject(audioDownloadUrl, byte[].class);
            Path tempAudioFile = Files.createTempFile("zoom_recording_", ".m4a");
            Files.write(tempAudioFile, audioBytes);

            String transcriptionText = audioTranscriptionService.transcribeAudio(tempAudioFile);

            Files.deleteIfExists(tempAudioFile);

            Transcription transcription = new Transcription();
            transcription.setMeetingId(meeting);
            transcription.setTranscriptionText(transcriptionText);
            transcription.setGeneratedAt(LocalDateTime.now());
            transcriptionRepository.save(transcription);

            SummaryRequest summaryRequest = new SummaryRequest();
            summaryRequest.setText(transcriptionText);
            String summaryText = summarizationService.generateSummaryFromText(summaryRequest).getSummaryText();

            meeting.setZoomRecordingUrl(audioDownloadUrl);
            meetingRepository.save(meeting);

            logger.info("Successfully processed recording for meeting: {}", meetingId);
        } catch (Exception e) {
            logger.error("Error processing recording: {}", e.getMessage(), e);
        }
    }

    private Optional<Meeting> findMeetingByZoomId(String zoomMeetingId) {
        return meetingRepository.findAll().stream()
                .filter(m -> zoomMeetingId.equals(m.getZoomMeetingId()))
                .findFirst();
    }
}